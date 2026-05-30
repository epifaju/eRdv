package com.erdv.dto;

import com.erdv.entity.PlageRecurrente;

import java.time.LocalTime;

public class PlageRecurrenteResponse {

    private Long id;
    private Long prestataireId;
    private Integer jourSemaine;
    private String jourLibelle;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private boolean actif;

    private static final String[] JOURS = {
            "", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"
    };

    public static PlageRecurrenteResponse from(PlageRecurrente p) {
        if (p == null) {
            return null;
        }
        PlageRecurrenteResponse r = new PlageRecurrenteResponse();
        r.id = p.getId();
        r.prestataireId = p.getPrestataire() != null ? p.getPrestataire().getId() : null;
        r.jourSemaine = p.getJourSemaine();
        if (p.getJourSemaine() != null && p.getJourSemaine() >= 1 && p.getJourSemaine() <= 7) {
            r.jourLibelle = JOURS[p.getJourSemaine()];
        }
        r.heureDebut = p.getHeureDebut();
        r.heureFin = p.getHeureFin();
        r.actif = p.isActif();
        return r;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPrestataireId() {
        return prestataireId;
    }

    public void setPrestataireId(Long prestataireId) {
        this.prestataireId = prestataireId;
    }

    public Integer getJourSemaine() {
        return jourSemaine;
    }

    public void setJourSemaine(Integer jourSemaine) {
        this.jourSemaine = jourSemaine;
    }

    public String getJourLibelle() {
        return jourLibelle;
    }

    public void setJourLibelle(String jourLibelle) {
        this.jourLibelle = jourLibelle;
    }

    public LocalTime getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(LocalTime heureDebut) {
        this.heureDebut = heureDebut;
    }

    public LocalTime getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(LocalTime heureFin) {
        this.heureFin = heureFin;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }
}
