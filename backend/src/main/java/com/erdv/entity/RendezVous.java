package com.erdv.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rendez_vous")
public class RendezVous {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "rendezVous" })
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestataire_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "rendezVous" })
    private Prestataire prestataire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etablissement_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Etablissement etablissement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creneau_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private CreneauHoraire creneau;

    @NotNull(message = "La date et heure sont obligatoires")
    @Column(nullable = false)
    private LocalDateTime dateHeure;

    @NotBlank(message = "Le service est obligatoire")
    @Column(nullable = false)
    private String service;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut statut = Statut.EN_ATTENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestation_id")
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "prestataire" })
    private Prestation prestation;

    @ManyToMany
    @JoinTable(
            name = "rendez_vous_creneaux",
            joinColumns = @JoinColumn(name = "rendez_vous_id"),
            inverseJoinColumns = @JoinColumn(name = "creneau_id"))
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "prestataire" })
    private List<CreneauHoraire> creneauxReserves = new ArrayList<>();

    @Column(name = "rappel_j1_envoye", nullable = false)
    private boolean rappelJ1Envoye = false;

    @Column(name = "rappel_h2_envoye", nullable = false)
    private boolean rappelH2Envoye = false;

    @Column(name = "rappel_j1_sms_envoye", nullable = false)
    private boolean rappelJ1SmsEnvoye = false;

    @Column(name = "rappel_h2_sms_envoye", nullable = false)
    private boolean rappelH2SmsEnvoye = false;

    public enum Statut {
        EN_ATTENTE, CONFIRME, ANNULE
    }

    // Constructeurs
    public RendezVous() {
    }

    public RendezVous(Utilisateur utilisateur, Prestataire prestataire, LocalDateTime dateHeure, String service) {
        this.utilisateur = utilisateur;
        this.prestataire = prestataire;
        this.dateHeure = dateHeure;
        this.service = service;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public Prestataire getPrestataire() {
        return prestataire;
    }

    public void setPrestataire(Prestataire prestataire) {
        this.prestataire = prestataire;
    }

    public Etablissement getEtablissement() {
        return etablissement;
    }

    public void setEtablissement(Etablissement etablissement) {
        this.etablissement = etablissement;
    }

    public CreneauHoraire getCreneau() {
        return creneau;
    }

    public void setCreneau(CreneauHoraire creneau) {
        this.creneau = creneau;
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

    public Statut getStatut() {
        return statut;
    }

    public void setStatut(Statut statut) {
        this.statut = statut;
    }

    public Prestation getPrestation() {
        return prestation;
    }

    public void setPrestation(Prestation prestation) {
        this.prestation = prestation;
    }

    public List<CreneauHoraire> getCreneauxReserves() {
        return creneauxReserves;
    }

    public void setCreneauxReserves(List<CreneauHoraire> creneauxReserves) {
        this.creneauxReserves = creneauxReserves != null ? creneauxReserves : new ArrayList<>();
    }

    public boolean isRappelJ1Envoye() {
        return rappelJ1Envoye;
    }

    public void setRappelJ1Envoye(boolean rappelJ1Envoye) {
        this.rappelJ1Envoye = rappelJ1Envoye;
    }

    public boolean isRappelH2Envoye() {
        return rappelH2Envoye;
    }

    public void setRappelH2Envoye(boolean rappelH2Envoye) {
        this.rappelH2Envoye = rappelH2Envoye;
    }

    public boolean isRappelJ1SmsEnvoye() {
        return rappelJ1SmsEnvoye;
    }

    public void setRappelJ1SmsEnvoye(boolean rappelJ1SmsEnvoye) {
        this.rappelJ1SmsEnvoye = rappelJ1SmsEnvoye;
    }

    public boolean isRappelH2SmsEnvoye() {
        return rappelH2SmsEnvoye;
    }

    public void setRappelH2SmsEnvoye(boolean rappelH2SmsEnvoye) {
        this.rappelH2SmsEnvoye = rappelH2SmsEnvoye;
    }
}