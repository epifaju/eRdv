package com.erdv.service;

import com.erdv.dto.PrestationRequest;
import com.erdv.dto.PrestationResponse;
import com.erdv.entity.Prestation;
import com.erdv.entity.Prestataire;
import com.erdv.entity.Utilisateur;
import com.erdv.exception.ApiException;
import com.erdv.repository.PrestationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PrestationService {

    @Autowired
    private PrestationRepository prestationRepository;

    @Autowired
    private PrestataireService prestataireService;

    @Autowired
    private PrestataireAccessService prestataireAccessService;

    @Transactional(readOnly = true)
    public List<PrestationResponse> listActivesByPrestataire(Long prestataireId) {
        prestataireService.getPrestataireEntityById(prestataireId);
        return prestationRepository.findByPrestataireIdAndActifTrueOrderByNomAsc(prestataireId).stream()
                .map(PrestationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PrestationResponse> listAllByPrestataire(Long prestataireId) {
        prestataireAccessService.assertCanManagePrestataire(prestataireId);
        return prestationRepository.findByPrestataireIdOrderByNomAsc(prestataireId).stream()
                .map(PrestationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Prestation getEntityById(Long id) {
        return prestationRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Prestation non trouvée"));
    }

    public PrestationResponse creer(PrestationRequest request) {
        Utilisateur user = prestataireAccessService.currentUser();
        Long prestataireId = prestataireAccessService.resolvePrestataireIdForWrite(user, request.getPrestataireId());
        Prestataire prestataire = prestataireService.getPrestataireEntityById(prestataireId);
        Prestation p = mapRequest(new Prestation(), request);
        p.setPrestataire(prestataire);
        return PrestationResponse.from(prestationRepository.save(p));
    }

    public PrestationResponse modifier(Long id, PrestationRequest request) {
        prestataireAccessService.assertCanManagePrestation(id);
        Prestation p = getEntityById(id);
        mapRequest(p, request);
        if (request.getPrestataireId() != null) {
            Long prestataireId = prestataireAccessService.resolvePrestataireIdForWrite(
                    prestataireAccessService.currentUser(), request.getPrestataireId());
            p.setPrestataire(prestataireService.getPrestataireEntityById(prestataireId));
        }
        return PrestationResponse.from(prestationRepository.save(p));
    }

    public void supprimer(Long id) {
        prestataireAccessService.assertCanManagePrestation(id);
        if (!prestationRepository.existsById(id)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Prestation non trouvée");
        }
        prestationRepository.deleteById(id);
    }

    private static Prestation mapRequest(Prestation p, PrestationRequest request) {
        p.setNom(request.getNom());
        p.setDescription(request.getDescription());
        p.setDureeMinutes(request.getDureeMinutes());
        p.setPrix(request.getPrix());
        if (request.getActif() != null) {
            p.setActif(request.getActif());
        }
        return p;
    }
}
