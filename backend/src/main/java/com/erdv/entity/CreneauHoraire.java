package com.erdv.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "creneaux_horaires")
public class CreneauHoraire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestataire_id", nullable = false)
    @JsonIgnoreProperties({ "creneaux", "rendezVous" })
    private Prestataire prestataire;

    @NotNull(message = "La date et heure sont obligatoires")
    @Column(nullable = false)
    private LocalDateTime dateHeure;

    @Column(nullable = false)
    private boolean disponible = true;

    // Constructeurs
    public CreneauHoraire() {
    }

    public CreneauHoraire(Prestataire prestataire, LocalDateTime dateHeure) {
        this.prestataire = prestataire;
        this.dateHeure = dateHeure;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Prestataire getPrestataire() {
        return prestataire;
    }

    public void setPrestataire(Prestataire prestataire) {
        this.prestataire = prestataire;
    }

    public LocalDateTime getDateHeure() {
        return dateHeure;
    }

    public void setDateHeure(LocalDateTime dateHeure) {
        this.dateHeure = dateHeure;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
}