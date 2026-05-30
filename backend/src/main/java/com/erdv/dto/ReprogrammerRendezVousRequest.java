package com.erdv.dto;

import jakarta.validation.constraints.NotNull;

public class ReprogrammerRendezVousRequest {

    @NotNull(message = "L'ID du nouveau créneau est obligatoire")
    private Long creneauId;

    public ReprogrammerRendezVousRequest() {
    }

    public ReprogrammerRendezVousRequest(Long creneauId) {
        this.creneauId = creneauId;
    }

    public Long getCreneauId() {
        return creneauId;
    }

    public void setCreneauId(Long creneauId) {
        this.creneauId = creneauId;
    }
}
