package com.erdv.service;

import com.erdv.dto.GenerationCreneauxResponse;
import com.erdv.entity.CreneauHoraire;
import com.erdv.entity.PlageRecurrente;
import com.erdv.entity.Prestataire;
import com.erdv.exception.ApiException;
import com.erdv.repository.CreneauHoraireRepository;
import com.erdv.repository.PlageRecurrenteRepository;
import com.erdv.repository.PrestationRepository;
import com.erdv.repository.PrestataireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class CreneauGenerationService {

    private static final int DEFAULT_GRANULARITE = 30;
    private static final int DEFAULT_HORIZON_JOURS = 28;

    @Autowired
    private PrestataireRepository prestataireRepository;

    @Autowired
    private PlageRecurrenteRepository plageRecurrenteRepository;

    @Autowired
    private PrestationRepository prestationRepository;

    @Autowired
    private CreneauHoraireRepository creneauHoraireRepository;

    @Value("${app.creneaux.horizon-jours:" + DEFAULT_HORIZON_JOURS + "}")
    private int horizonJours;

    @Transactional
    public GenerationCreneauxResponse genererPourPrestataire(Long prestataireId, Integer jours) {
        Prestataire prestataire = prestataireRepository.findById(prestataireId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Prestataire non trouvé"));

        List<PlageRecurrente> plages = plageRecurrenteRepository
                .findByPrestataireIdAndActifTrueOrderByJourSemaineAscHeureDebutAsc(prestataireId);
        if (plages.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Aucune plage récurrente active. Définissez les horaires d'ouverture d'abord.");
        }

        int granularite = prestationRepository.findMinDureeActive(prestataireId).orElse(DEFAULT_GRANULARITE);
        int nbJours = jours != null && jours > 0 ? jours : horizonJours;
        LocalDateTime now = LocalDateTime.now();
        int created = 0;

        for (int d = 0; d < nbJours; d++) {
            LocalDate date = now.toLocalDate().plusDays(d);
            int isoDow = date.getDayOfWeek().getValue();

            for (PlageRecurrente plage : plages) {
                if (!plage.getJourSemaine().equals(isoDow)) {
                    continue;
                }
                LocalTime cursor = plage.getHeureDebut();
                while (cursor.plusMinutes(granularite).compareTo(plage.getHeureFin()) <= 0) {
                    LocalDateTime slotStart = LocalDateTime.of(date, cursor);
                    if (slotStart.isBefore(now)) {
                        cursor = cursor.plusMinutes(granularite);
                        continue;
                    }
                    if (!creneauHoraireRepository.existsByPrestataireIdAndDateHeure(prestataire.getId(), slotStart)) {
                        CreneauHoraire creneau = new CreneauHoraire();
                        creneau.setPrestataire(prestataire);
                        creneau.setDateHeure(slotStart);
                        creneau.setDureeMinutes(granularite);
                        creneau.setDisponible(true);
                        creneauHoraireRepository.save(creneau);
                        created++;
                    }
                    cursor = cursor.plusMinutes(granularite);
                }
            }
        }

        return new GenerationCreneauxResponse(created, granularite, nbJours);
    }

    @Transactional
    public int genererPourTousLesPrestataires() {
        int total = 0;
        for (Prestataire p : prestataireRepository.findAll()) {
            try {
                total += genererPourPrestataire(p.getId(), horizonJours).getCreneauxCrees();
            } catch (ApiException ex) {
                if (ex.getStatus() != HttpStatus.BAD_REQUEST) {
                    throw ex;
                }
            }
        }
        return total;
    }
}
