package com.erdv.dto;

import com.erdv.entity.Prestation;

import java.math.BigDecimal;

public class PrestationResponse {

    private Long id;
    private Long prestataireId;
    private String nom;
    private String description;
    private Integer dureeMinutes;
    private BigDecimal prix;
    private boolean actif;

    public static PrestationResponse from(Prestation p) {
        if (p == null) {
            return null;
        }
        PrestationResponse r = new PrestationResponse();
        r.id = p.getId();
        r.prestataireId = p.getPrestataire() != null ? p.getPrestataire().getId() : null;
        r.nom = p.getNom();
        r.description = p.getDescription();
        r.dureeMinutes = p.getDureeMinutes();
        r.prix = p.getPrix();
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

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDureeMinutes() {
        return dureeMinutes;
    }

    public void setDureeMinutes(Integer dureeMinutes) {
        this.dureeMinutes = dureeMinutes;
    }

    public BigDecimal getPrix() {
        return prix;
    }

    public void setPrix(BigDecimal prix) {
        this.prix = prix;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }
}
