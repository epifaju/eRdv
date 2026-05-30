package com.erdv.service;

import com.erdv.entity.CreneauHoraire;
import com.erdv.entity.Prestataire;
import com.erdv.entity.RendezVous;
import com.erdv.exception.ApiException;
import com.erdv.repository.CreneauHoraireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public List<CreneauHoraire> getCreneauxDisponiblesForDate(Long prestataireId, LocalDate date) {
        return getCreneauxDisponiblesForDate(prestataireId, date, null);
    }

    public List<CreneauHoraire> getCreneauxDisponiblesForDate(Long prestataireId, LocalDate date,
            Integer dureePrestationMinutes) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        List<CreneauHoraire> creneaux = creneauHoraireRepository.findCreneauxDisponiblesBetween(
                prestataireId, start, end);
        if (dureePrestationMinutes == null || dureePrestationMinutes <= 0) {
            return creneaux;
        }
        return filterDebutCreneauxConsecutifs(creneaux, dureePrestationMinutes);
    }

    public CreneauHoraire getCreneauById(Long id) {
        return creneauHoraireRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Créneau non trouvé"));
    }

    public int computeSlotsNeeded(int dureePrestationMinutes, int granulariteMinutes) {
        if (granulariteMinutes <= 0) {
            return 1;
        }
        return (int) Math.ceil((double) dureePrestationMinutes / granulariteMinutes);
    }

    /**
     * Vérifie la disponibilité sans réserver (pré-contrôle avant paiement).
     */
    @Transactional(readOnly = true)
    public void assertCreneauxDisponiblesConsecutifs(Long premierCreneauId, int nbSlots) {
        if (nbSlots <= 1) {
            CreneauHoraire creneau = creneauHoraireRepository.findById(premierCreneauId)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Créneau non trouvé"));
            if (!creneau.isDisponible()) {
                throw new ApiException(HttpStatus.CONFLICT, "Ce créneau n'est plus disponible");
            }
            return;
        }

        CreneauHoraire first = creneauHoraireRepository.findById(premierCreneauId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Créneau non trouvé"));
        if (!first.isDisponible()) {
            throw new ApiException(HttpStatus.CONFLICT, "Ce créneau n'est plus disponible");
        }

        LocalDateTime nextStart = first.getDateHeure().plusMinutes(first.getDureeMinutes());
        for (int i = 1; i < nbSlots; i++) {
            final LocalDateTime expectedStart = nextStart;
            CreneauHoraire next = creneauHoraireRepository
                    .findByPrestataireIdAndDateHeure(first.getPrestataire().getId(), expectedStart)
                    .orElseGet(() -> creneauHoraireRepository.findCreneauxDisponiblesBetween(
                            first.getPrestataire().getId(),
                            expectedStart,
                            expectedStart.plusMinutes(1))
                            .stream().findFirst().orElse(null));
            if (next == null || !next.isDisponible()) {
                throw new ApiException(HttpStatus.CONFLICT,
                        "Durée insuffisante : les créneaux consécutifs ne sont pas tous disponibles");
            }
            nextStart = next.getDateHeure().plusMinutes(next.getDureeMinutes());
        }
    }

    /**
     * Verrouille le créneau en base et le marque indisponible (anti double-réservation).
     */
    @Transactional
    public CreneauHoraire reserverCreneau(Long id) {
        CreneauHoraire creneau = creneauHoraireRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Créneau non trouvé"));
        if (!creneau.isDisponible()) {
            throw new ApiException(HttpStatus.CONFLICT, "Ce créneau n'est plus disponible");
        }
        creneau.setDisponible(false);
        return creneauHoraireRepository.save(creneau);
    }

    @Transactional
    public List<CreneauHoraire> reserverCreneauxConsecutifs(Long premierCreneauId, int nbSlots) {
        if (nbSlots <= 1) {
            return new ArrayList<>(List.of(reserverCreneau(premierCreneauId)));
        }

        CreneauHoraire first = creneauHoraireRepository.findByIdForUpdate(premierCreneauId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Créneau non trouvé"));
        if (!first.isDisponible()) {
            throw new ApiException(HttpStatus.CONFLICT, "Ce créneau n'est plus disponible");
        }

        List<CreneauHoraire> chain = new ArrayList<>();
        chain.add(first);
        LocalDateTime nextStart = first.getDateHeure().plusMinutes(first.getDureeMinutes());

        for (int i = 1; i < nbSlots; i++) {
            final LocalDateTime expectedStart = nextStart;
            CreneauHoraire next = creneauHoraireRepository
                    .findByPrestataireIdAndDateHeure(first.getPrestataire().getId(), expectedStart)
                    .orElseGet(() -> creneauHoraireRepository.findCreneauxDisponiblesBetween(
                            first.getPrestataire().getId(),
                            expectedStart,
                            expectedStart.plusMinutes(1))
                            .stream().findFirst().orElse(null));
            if (next == null || !next.isDisponible()) {
                throw new ApiException(HttpStatus.CONFLICT,
                        "Durée insuffisante : les créneaux consécutifs ne sont pas tous disponibles");
            }
            chain.add(next);
            nextStart = next.getDateHeure().plusMinutes(next.getDureeMinutes());
        }

        List<CreneauHoraire> reserved = new ArrayList<>();
        try {
            for (CreneauHoraire slot : chain) {
                reserved.add(reserverCreneau(slot.getId()));
            }
        } catch (RuntimeException ex) {
            for (CreneauHoraire r : reserved) {
                marquerCreneauDisponible(r.getId());
            }
            throw ex;
        }
        return reserved;
    }

    public void libererCreneauxRendezVous(RendezVous rendezVous) {
        if (rendezVous.getCreneauxReserves() != null && !rendezVous.getCreneauxReserves().isEmpty()) {
            for (CreneauHoraire c : rendezVous.getCreneauxReserves()) {
                marquerCreneauDisponible(c.getId());
            }
            return;
        }
        if (rendezVous.getCreneau() != null) {
            marquerCreneauDisponible(rendezVous.getCreneau().getId());
        }
    }

    public CreneauHoraire creerCreneau(CreneauHoraire creneau) {
        Prestataire prestataire = prestataireService.getPrestataireEntityById(creneau.getPrestataire().getId());
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

    private List<CreneauHoraire> filterDebutCreneauxConsecutifs(List<CreneauHoraire> creneaux,
            int dureePrestationMinutes) {
        if (creneaux.isEmpty()) {
            return creneaux;
        }
        int granularite = creneaux.get(0).getDureeMinutes();
        int needed = computeSlotsNeeded(dureePrestationMinutes, granularite);
        if (needed <= 1) {
            return creneaux;
        }

        Map<LocalDateTime, CreneauHoraire> byStart = new HashMap<>();
        for (CreneauHoraire c : creneaux) {
            byStart.put(c.getDateHeure(), c);
        }

        List<CreneauHoraire> starts = new ArrayList<>();
        for (CreneauHoraire start : creneaux) {
            if (hasConsecutiveChain(start, needed, byStart)) {
                starts.add(start);
            }
        }
        return starts;
    }

    private boolean hasConsecutiveChain(CreneauHoraire start, int needed,
            Map<LocalDateTime, CreneauHoraire> byStart) {
        LocalDateTime cursor = start.getDateHeure();
        CreneauHoraire current = start;
        for (int i = 1; i < needed; i++) {
            cursor = cursor.plusMinutes(current.getDureeMinutes());
            current = byStart.get(cursor);
            if (current == null) {
                return false;
            }
        }
        return true;
    }
}
