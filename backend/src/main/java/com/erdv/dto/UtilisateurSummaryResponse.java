package com.erdv.dto;

import com.erdv.entity.Utilisateur;

public class UtilisateurSummaryResponse {

    private Long id;
    private String nom;
    private String email;
    private String telephone;

    public static UtilisateurSummaryResponse from(Utilisateur u) {
        if (u == null) {
            return null;
        }
        UtilisateurSummaryResponse r = new UtilisateurSummaryResponse();
        r.id = u.getId();
        r.nom = u.getNom();
        r.email = u.getEmail();
        r.telephone = u.getTelephone();
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
}
