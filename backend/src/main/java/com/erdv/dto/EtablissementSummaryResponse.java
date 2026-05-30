package com.erdv.dto;

import com.erdv.entity.Etablissement;

public class EtablissementSummaryResponse {

    private Long id;
    private String nom;
    private String ville;
    private String libelleComplet;

    public static EtablissementSummaryResponse from(Etablissement e) {
        if (e == null) {
            return null;
        }
        EtablissementSummaryResponse r = new EtablissementSummaryResponse();
        r.id = e.getId();
        r.nom = e.getNom();
        r.ville = e.getVille();
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

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getLibelleComplet() {
        return libelleComplet;
    }

    public void setLibelleComplet(String libelleComplet) {
        this.libelleComplet = libelleComplet;
    }
}
