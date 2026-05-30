package com.erdv.dto;

import com.erdv.entity.Prestataire;

public class PrestataireResponse {

    private Long id;
    private String nom;
    private String specialite;
    private String email;
    private Long etablissementId;
    private String etablissementNom;
    private String etablissementVille;

    public static PrestataireResponse from(Prestataire p) {
        if (p == null) {
            return null;
        }
        PrestataireResponse r = new PrestataireResponse();
        r.id = p.getId();
        r.nom = p.getNom();
        r.specialite = p.getSpecialite();
        r.email = p.getEmail();
        if (p.getEtablissement() != null) {
            r.etablissementId = p.getEtablissement().getId();
            r.etablissementNom = p.getEtablissement().getNom();
            r.etablissementVille = p.getEtablissement().getVille();
        }
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

    public Long getEtablissementId() {
        return etablissementId;
    }

    public void setEtablissementId(Long etablissementId) {
        this.etablissementId = etablissementId;
    }

    public String getEtablissementNom() {
        return etablissementNom;
    }

    public void setEtablissementNom(String etablissementNom) {
        this.etablissementNom = etablissementNom;
    }

    public String getEtablissementVille() {
        return etablissementVille;
    }

    public void setEtablissementVille(String etablissementVille) {
        this.etablissementVille = etablissementVille;
    }
}
