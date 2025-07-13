package com.erdv.service;

import com.erdv.entity.CreneauHoraire;
import com.erdv.entity.Prestataire;
import com.erdv.repository.CreneauHoraireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CreneauHoraireService {

    @Autowired
    private CreneauHoraireRepository creneauHoraireRepository;

    @Autowired
    private PrestataireService prestataireService;

    public List<CreneauHoraire> getAllCreneaux() {
        return creneauHoraireRepository.findAll();
    }

    public List<CreneauHoraire> getCreneauxByPrestataire(Long prestataireId) {
        return creneauHoraireRepository.findByPrestataireId(prestataireId);
    }

    public List<CreneauHoraire> getCreneauxDisponibles(Long prestataireId) {
        return creneauHoraireRepository.findByPrestataireIdAndDisponibleTrueOrderByDateHeure(prestataireId);
    }

    public List<CreneauHoraire> getCreneauxDisponibles(Long prestataireId, LocalDateTime dateDebut) {
        return creneauHoraireRepository.findCreneauxDisponibles(prestataireId, dateDebut);
    }

    public CreneauHoraire getCreneauById(Long id) {
        return creneauHoraireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Créneau non trouvé"));
    }

    public CreneauHoraire creerCreneau(CreneauHoraire creneau) {
        Prestataire prestataire = prestataireService.getPrestataireById(creneau.getPrestataire().getId());
        creneau.setPrestataire(prestataire);
        return creneauHoraireRepository.save(creneau);
    }

    public CreneauHoraire updateCreneau(Long id, CreneauHoraire creneauDetails) {
        CreneauHoraire creneau = getCreneauById(id);

        creneau.setDateHeure(creneauDetails.getDateHeure());
        creneau.setDisponible(creneauDetails.isDisponible());

        return creneauHoraireRepository.save(creneau);
    }

    public void deleteCreneau(Long id) {
        creneauHoraireRepository.deleteById(id);
    }

    public void marquerCreneauIndisponible(Long id) {
        CreneauHoraire creneau = getCreneauById(id);
        creneau.setDisponible(false);
        creneauHoraireRepository.save(creneau);
    }

    public void marquerCreneauDisponible(Long id) {
        CreneauHoraire creneau = getCreneauById(id);
        creneau.setDisponible(true);
        creneauHoraireRepository.save(creneau);
    }
}