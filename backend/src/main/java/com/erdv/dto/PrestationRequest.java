package com.erdv.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class PrestationRequest {

    @NotNull(message = "L'ID du prestataire est obligatoire")
    private Long prestataireId;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 255)
    private String nom;

    @Size(max = 1000)
    private String description;

    @NotNull(message = "La durée est obligatoire")
    @Min(value = 15, message = "Durée minimale : 15 minutes")
    private Integer dureeMinutes = 30;

    private BigDecimal prix;

    private Boolean actif = true;

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

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }
}
