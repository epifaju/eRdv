package com.erdv.dto;

import com.erdv.entity.CreneauHoraire;

import java.time.LocalDateTime;

public class CreneauSummaryResponse {

    private Long id;
    private LocalDateTime dateHeure;
    private int dureeMinutes;
    private boolean disponible;
    private PrestataireSummaryResponse prestataire;

    public static CreneauSummaryResponse from(CreneauHoraire c) {
        if (c == null) {
            return null;
        }
        CreneauSummaryResponse r = new CreneauSummaryResponse();
        r.id = c.getId();
        r.dateHeure = c.getDateHeure();
        r.dureeMinutes = c.getDureeMinutes();
        r.disponible = c.isDisponible();
        r.prestataire = PrestataireSummaryResponse.from(c.getPrestataire());
        return r;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDateHeure() {
        return dateHeure;
    }

    public void setDateHeure(LocalDateTime dateHeure) {
        this.dateHeure = dateHeure;
    }

    public int getDureeMinutes() {
        return dureeMinutes;
    }

    public void setDureeMinutes(int dureeMinutes) {
        this.dureeMinutes = dureeMinutes;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public PrestataireSummaryResponse getPrestataire() {
        return prestataire;
    }

    public void setPrestataire(PrestataireSummaryResponse prestataire) {
        this.prestataire = prestataire;
    }
}
