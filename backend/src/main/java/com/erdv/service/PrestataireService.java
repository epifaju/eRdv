package com.erdv.service;

import com.erdv.dto.PrestataireRequest;
import com.erdv.dto.PrestataireResponse;
import com.erdv.entity.Prestataire;
import com.erdv.exception.ApiException;
import com.erdv.repository.EtablissementRepository;
import com.erdv.repository.PrestataireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PrestataireService {

    @Autowired
    private PrestataireRepository prestataireRepository;

    @Autowired
    private EtablissementRepository etablissementRepository;

    @Transactional(readOnly = true)
    public List<PrestataireResponse> getAllPrestataires(Long etablissementId) {
        List<Prestataire> list;
        if (etablissementId != null) {
            list = prestataireRepository.findByEtablissement_IdOrderByNomAsc(etablissementId);
        } else {
            list = prestataireRepository.findAllWithEtablissement();
        }
        return list.stream().map(PrestataireResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<PrestataireResponse> getPrestatairesByEtablissement(Long etablissementId) {
        return prestataireRepository.findByEtablissement_IdOrderByNomAsc(etablissementId).stream()
                .map(PrestataireResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PrestataireResponse getPrestataireById(Long id) {
        return PrestataireResponse.from(getPrestataireEntityById(id));
    }

    @Transactional(readOnly = true)
    public Prestataire getPrestataireEntityById(Long id) {
        return prestataireRepository.findByIdWithEtablissement(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Prestataire non trouvé"));
    }

    public PrestataireResponse creerPrestataire(PrestataireRequest request) {
        return PrestataireResponse.from(creerPrestataireEntity(request));
    }

    public Prestataire creerPrestataireEntity(PrestataireRequest request) {
        var etab = etablissementRepository.findById(request.getEtablissementId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Établissement invalide"));
        Prestataire prestataire = new Prestataire();
        prestataire.setNom(request.getNom());
        prestataire.setSpecialite(request.getSpecialite());
        prestataire.setEmail(request.getEmail());
        prestataire.setEtablissement(etab);
        return prestataireRepository.save(prestataire);
    }

    public PrestataireResponse updatePrestataire(Long id, PrestataireRequest request) {
        return PrestataireResponse.from(updatePrestataireEntity(id, request));
    }

    public Prestataire updatePrestataireEntity(Long id, PrestataireRequest request) {
        Prestataire prestataire = getPrestataireEntityById(id);

        prestataire.setNom(request.getNom());
        prestataire.setSpecialite(request.getSpecialite());
        prestataire.setEmail(request.getEmail());
        var etab = etablissementRepository.findById(request.getEtablissementId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Établissement invalide"));
        prestataire.setEtablissement(etab);

        return prestataireRepository.save(prestataire);
    }

    public void deletePrestataire(Long id) {
        prestataireRepository.deleteById(id);
    }
}
