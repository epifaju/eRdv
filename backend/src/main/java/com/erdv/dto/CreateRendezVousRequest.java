package com.erdv.dto;

import com.erdv.entity.RendezVous;
import jakarta.validation.constraints.NotNull;

public class CreateRendezVousRequest {

    @NotNull(message = "L'ID du créneau est obligatoire")
    private Long creneauId;

    /** Si renseigné, le libellé service est dérivé de la prestation. */
    private Long prestationId;

    private String service;

    private RendezVous.Statut statut = RendezVous.Statut.EN_ATTENTE;

    public CreateRendezVousRequest() {
    }

    public CreateRendezVousRequest(Long creneauId, String service) {
        this.creneauId = creneauId;
        this.service = service;
    }

    public Long getCreneauId() {
        return creneauId;
    }

    public void setCreneauId(Long creneauId) {
        this.creneauId = creneauId;
    }

    public Long getPrestationId() {
        return prestationId;
    }

    public void setPrestationId(Long prestationId) {
        this.prestationId = prestationId;
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
