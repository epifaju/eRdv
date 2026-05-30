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

    /** EAGER : le prestataire est toujours inclus dans les réponses JSON (pas de lazy hors session). */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "prestataire_id", nullable = false)
    @JsonIgnoreProperties({ "creneaux", "rendezVous" })
    private Prestataire prestataire;

    @NotNull(message = "La date et heure sont obligatoires")
    @Column(nullable = false)
    private LocalDateTime dateHeure;

    @Column(nullable = false)
    private boolean disponible = true;

    @Column(name = "duree_minutes", nullable = false)
    private int dureeMinutes = 30;

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

    public int getDureeMinutes() {
        return dureeMinutes;
    }

    public void setDureeMinutes(int dureeMinutes) {
        this.dureeMinutes = dureeMinutes;
    }
}