package com.erdv.service;

import com.erdv.entity.Prestataire;
import com.erdv.exception.ApiException;
import com.erdv.repository.EtablissementRepository;
import com.erdv.repository.PrestataireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrestataireService {

    @Autowired
    private PrestataireRepository prestataireRepository;

    @Autowired
    private EtablissementRepository etablissementRepository;

    public List<Prestataire> getAllPrestataires(Long etablissementId) {
        if (etablissementId != null) {
            return prestataireRepository.findByEtablissement_IdOrderByNomAsc(etablissementId);
        }
        return prestataireRepository.findAll();
    }

    public List<Prestataire> getPrestatairesByEtablissement(Long etablissementId) {
        return prestataireRepository.findByEtablissement_IdOrderByNomAsc(etablissementId);
    }

    public Prestataire getPrestataireById(Long id) {
        return prestataireRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Prestataire non trouvé"));
    }

    public Prestataire creerPrestataire(Prestataire prestataire) {
        if (prestataire.getEtablissement() == null || prestataire.getEtablissement().getId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "L'établissement est obligatoire");
        }
        var etab = etablissementRepository.findById(prestataire.getEtablissement().getId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Établissement invalide"));
        prestataire.setEtablissement(etab);
        return prestataireRepository.save(prestataire);
    }

    public Prestataire updatePrestataire(Long id, Prestataire prestataireDetails) {
        Prestataire prestataire = getPrestataireById(id);

        prestataire.setNom(prestataireDetails.getNom());
        prestataire.setSpecialite(prestataireDetails.getSpecialite());
        prestataire.setEmail(prestataireDetails.getEmail());
        if (prestataireDetails.getEtablissement() != null && prestataireDetails.getEtablissement().getId() != null) {
            var etab = etablissementRepository.findById(prestataireDetails.getEtablissement().getId())
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Établissement invalide"));
            prestataire.setEtablissement(etab);
        }

        return prestataireRepository.save(prestataire);
    }

    public void deletePrestataire(Long id) {
        prestataireRepository.deleteById(id);
    }
}
