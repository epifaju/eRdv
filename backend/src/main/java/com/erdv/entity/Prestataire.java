package com.erdv.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

@Entity
@Table(name = "prestataires")
public class Prestataire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    @Column(nullable = false)
    private String nom;

    @NotBlank(message = "La spécialité est obligatoire")
    @Size(min = 2, max = 100, message = "La spécialité doit contenir entre 2 et 100 caractères")
    @Column(nullable = false)
    private String specialite;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Column(unique = true, nullable = false)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "etablissement_id", nullable = false)
    @JsonIgnore
    private Etablissement etablissement;

    /** Non sérialisé : évite LazyInitializationException (open-in-view désactivé) et références circulaires. */
    @JsonIgnore
    @OneToMany(mappedBy = "prestataire", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CreneauHoraire> creneaux;

    @OneToMany(mappedBy = "prestataire", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<RendezVous> rendezVous;

    // Constructeurs
    public Prestataire() {
    }

    public Prestataire(String nom, String specialite, String email) {
        this.nom = nom;
        this.specialite = specialite;
        this.email = email;
    }

    // Getters et Setters
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

    public String getSpecialite() {
        return specialite;
    }

    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Etablissement getEtablissement() {
        return etablissement;
    }

    public void setEtablissement(Etablissement etablissement) {
        this.etablissement = etablissement;
    }

    public Long getEtablissementId() {
        return etablissement != null ? etablissement.getId() : null;
    }

    public String getEtablissementNom() {
        return etablissement != null ? etablissement.getNom() : null;
    }

    public String getEtablissementVille() {
        return etablissement != null ? etablissement.getVille() : null;
    }

    public void setEtablissementId(Long etablissementId) {
        if (etablissementId == null) {
            return;
        }
        Etablissement e = new Etablissement();
        e.setId(etablissementId);
        this.etablissement = e;
    }

    public List<CreneauHoraire> getCreneaux() {
        return creneaux;
    }

    public void setCreneaux(List<CreneauHoraire> creneaux) {
        this.creneaux = creneaux;
    }

    public List<RendezVous> getRendezVous() {
        return rendezVous;
    }

    public void setRendezVous(List<RendezVous> rendezVous) {
        this.rendezVous = rendezVous;
    }
}