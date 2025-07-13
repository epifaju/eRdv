package com.erdv.dto;

import com.erdv.entity.RendezVous;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class CreateRendezVousRequest {

    @NotNull(message = "L'ID du prestataire est obligatoire")
    private Long prestataireId;

    @NotNull(message = "La date et heure sont obligatoires")
    private LocalDateTime dateHeure;

    @NotBlank(message = "Le service est obligatoire")
    private String service;

    private RendezVous.Statut statut = RendezVous.Statut.EN_ATTENTE;

    // Constructeurs
    public CreateRendezVousRequest() {
    }

    public CreateRendezVousRequest(Long prestataireId, LocalDateTime dateHeure, String service) {
        this.prestataireId = prestataireId;
        this.dateHeure = dateHeure;
        this.service = service;
    }

    // Getters et Setters
    public Long getPrestataireId() {
        return prestataireId;
    }

    public void setPrestataireId(Long prestataireId) {
        this.prestataireId = prestataireId;
    }

    public LocalDateTime getDateHeure() {
        return dateHeure;
    }

    public void setDateHeure(LocalDateTime dateHeure) {
        this.dateHeure = dateHeure;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public RendezVous.Statut getStatut() {
        return statut;
    }

    public void setStatut(RendezVous.Statut statut) {
        this.statut = statut;
    }
}