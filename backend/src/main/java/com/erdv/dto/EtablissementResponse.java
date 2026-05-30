package com.erdv.dto;

import com.erdv.entity.Etablissement;

public class EtablissementResponse {

    private Long id;
    private String nom;
    private String adresse;
    private String ville;
    private String codePostal;
    private String telephone;
    private boolean actif;
    private String libelleComplet;

    public static EtablissementResponse from(Etablissement e) {
        if (e == null) {
            return null;
        }
        EtablissementResponse r = new EtablissementResponse();
        r.id = e.getId();
        r.nom = e.getNom();
        r.adresse = e.getAdresse();
        r.ville = e.getVille();
        r.codePostal = e.getCodePostal();
        r.telephone = e.getTelephone();
        r.actif = e.isActif();
        r.libelleComplet = e.getLibelleComplet();
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

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getCodePostal() {
        return codePostal;
    }

    public void setCodePostal(String codePostal) {
        this.codePostal = codePostal;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public String getLibelleComplet() {
        return libelleComplet;
    }

    public void setLibelleComplet(String libelleComplet) {
        this.libelleComplet = libelleComplet;
    }
}
