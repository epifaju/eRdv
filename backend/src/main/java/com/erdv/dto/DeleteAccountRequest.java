package com.erdv.dto;

import com.erdv.entity.RendezVous;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DeleteAccountRequest {

    @NotBlank(message = "Le mot de passe est obligatoire pour confirmer la suppression")
    private String motDePasse;

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }
}
