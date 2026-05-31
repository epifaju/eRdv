package com.erdv.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 50)
    private String nom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @NotBlank(message = "Le téléphone est obligatoire")
    private String telephone;

    private Boolean consentementSmsRappels;

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

    public Boolean getConsentementSmsRappels() {
        return consentementSmsRappels;
    }

    public void setConsentementSmsRappels(Boolean consentementSmsRappels) {
        this.consentementSmsRappels = consentementSmsRappels;
    }
}
