package com.erdv.dto;

import com.erdv.entity.Prestataire;

public class PrestataireSummaryResponse {

    private Long id;
    private String nom;
    private String specialite;
    private String email;

    public static PrestataireSummaryResponse from(Prestataire p) {
        if (p == null) {
            return null;
        }
        PrestataireSummaryResponse r = new PrestataireSummaryResponse();
        r.id = p.getId();
        r.nom = p.getNom();
        r.specialite = p.getSpecialite();
        r.email = p.getEmail();
        return r;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getSpecialite() {
        return specialite;
    }

    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
