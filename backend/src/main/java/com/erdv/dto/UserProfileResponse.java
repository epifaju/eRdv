package com.erdv.dto;

import com.erdv.entity.Utilisateur;

public class UserProfileResponse {

    private Long id;
    private String nom;
    private String email;
    private String telephone;
    private String role;
    private Long prestataireId;
    private String prestataireNom;

    public static UserProfileResponse from(Utilisateur u) {
        UserProfileResponse r = new UserProfileResponse();
        r.setId(u.getId());
        r.setNom(u.getNom());
        r.setEmail(u.getEmail());
        r.setTelephone(u.getTelephone());
        r.setRole(u.getRole().name());
        if (u.getPrestataire() != null) {
            r.setPrestataireId(u.getPrestataire().getId());
            r.setPrestataireNom(u.getPrestataire().getNom());
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getPrestataireId() {
        return prestataireId;
    }

    public void setPrestataireId(Long prestataireId) {
        this.prestataireId = prestataireId;
    }

    public String getPrestataireNom() {
        return prestataireNom;
    }

    public void setPrestataireNom(String prestataireNom) {
        this.prestataireNom = prestataireNom;
    }
}
