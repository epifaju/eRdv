package com.erdv.service;

import com.erdv.entity.Prestataire;
import com.erdv.repository.PrestataireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrestataireService {

    @Autowired
    private PrestataireRepository prestataireRepository;

    public List<Prestataire> getAllPrestataires() {
        return prestataireRepository.findAll();
    }

    public Prestataire getPrestataireById(Long id) {
        return prestataireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prestataire non trouvé"));
    }

    public Prestataire creerPrestataire(Prestataire prestataire) {
        return prestataireRepository.save(prestataire);
    }

    public Prestataire updatePrestataire(Long id, Prestataire prestataireDetails) {
        Prestataire prestataire = getPrestataireById(id);

        prestataire.setNom(prestataireDetails.getNom());
        prestataire.setSpecialite(prestataireDetails.getSpecialite());
        prestataire.setEmail(prestataireDetails.getEmail());

        return prestataireRepository.save(prestataire);
    }

    public void deletePrestataire(Long id) {
        prestataireRepository.deleteById(id);
    }
}