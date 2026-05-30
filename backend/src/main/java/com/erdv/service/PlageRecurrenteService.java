package com.erdv.service;

import com.erdv.dto.GenerationCreneauxResponse;
import com.erdv.dto.PlageRecurrenteRequest;
import com.erdv.dto.PlageRecurrenteResponse;
import com.erdv.entity.PlageRecurrente;
import com.erdv.entity.Prestataire;
import com.erdv.entity.Utilisateur;
import com.erdv.exception.ApiException;
import com.erdv.repository.PlageRecurrenteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PlageRecurrenteService {

    @Autowired
    private PlageRecurrenteRepository plageRecurrenteRepository;

    @Autowired
    private PrestataireService prestataireService;

    @Autowired
    private CreneauGenerationService creneauGenerationService;

    @Autowired
    private PrestataireAccessService prestataireAccessService;

    @Transactional(readOnly = true)
    public List<PlageRecurrenteResponse> listByPrestataire(Long prestataireId) {
        prestataireAccessService.assertCanManagePrestataire(prestataireId);
        return plageRecurrenteRepository.findByPrestataireIdOrderByJourSemaineAscHeureDebutAsc(prestataireId).stream()
                .map(PlageRecurrenteResponse::from)
                .toList();
    }

    public PlageRecurrenteResponse creer(PlageRecurrenteRequest request) {
        validateHeures(request);
        Utilisateur user = prestataireAccessService.currentUser();
        Long prestataireId = prestataireAccessService.resolvePrestataireIdForWrite(user, request.getPrestataireId());
        Prestataire prestataire = prestataireService.getPrestataireEntityById(prestataireId);
        PlageRecurrente plage = mapRequest(new PlageRecurrente(), request);
        plage.setPrestataire(prestataire);
        return PlageRecurrenteResponse.from(plageRecurrenteRepository.save(plage));
    }

    public PlageRecurrenteResponse modifier(Long id, PlageRecurrenteRequest request) {
        validateHeures(request);
        prestataireAccessService.assertCanManagePlage(id);
        PlageRecurrente plage = plageRecurrenteRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Plage horaire non trouvée"));
        mapRequest(plage, request);
        if (request.getPrestataireId() != null) {
            Long prestataireId = prestataireAccessService.resolvePrestataireIdForWrite(
                    prestataireAccessService.currentUser(), request.getPrestataireId());
            plage.setPrestataire(prestataireService.getPrestataireEntityById(prestataireId));
        }
        return PlageRecurrenteResponse.from(plageRecurrenteRepository.save(plage));
    }

    public void supprimer(Long id) {
        prestataireAccessService.assertCanManagePlage(id);
        if (!plageRecurrenteRepository.existsById(id)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Plage horaire non trouvée");
        }
        plageRecurrenteRepository.deleteById(id);
    }

    public GenerationCreneauxResponse genererCreneaux(Long prestataireId, Integer jours) {
        prestataireAccessService.assertCanManagePrestataire(prestataireId);
        return creneauGenerationService.genererPourPrestataire(prestataireId, jours);
    }

    private static void validateHeures(PlageRecurrenteRequest request) {
        if (request.getHeureFin().compareTo(request.getHeureDebut()) <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "L'heure de fin doit être après l'heure de début");
        }
    }

    private static PlageRecurrente mapRequest(PlageRecurrente plage, PlageRecurrenteRequest request) {
        plage.setJourSemaine(request.getJourSemaine());
        plage.setHeureDebut(request.getHeureDebut());
        plage.setHeureFin(request.getHeureFin());
        if (request.getActif() != null) {
            plage.setActif(request.getActif());
        }
        return plage;
    }
}
