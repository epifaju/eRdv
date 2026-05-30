package com.erdv.dto;

import com.erdv.entity.Etablissement;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class EtablissementRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 255)
    private String nom;

    @Size(max = 500)
    private String adresse;

    @Size(max = 100)
    private String ville;

    @Size(max = 20)
    private String codePostal;

    @Size(max = 50)
    private String telephone;

    private Boolean actif;

    public void applyTo(Etablissement e) {
        e.setNom(nom);
        e.setAdresse(adresse);
        e.setVille(ville);
        e.setCodePostal(codePostal);
        e.setTelephone(telephone);
        if (actif != null) {
            e.setActif(actif);
        }
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getCodePostal() {
        return codePostal;
    }

    public void setCodePostal(String codePostal) {
        this.codePostal = codePostal;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }
}
