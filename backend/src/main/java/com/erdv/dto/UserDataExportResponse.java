package com.erdv.dto;

import java.time.Instant;
import java.util.List;

public class UserDataExportResponse {

    private Instant exportedAt;
    private UserProfileResponse profil;
    private List<RendezVousResponse> rendezVous;

    public Instant getExportedAt() {
        return exportedAt;
    }

    public void setExportedAt(Instant exportedAt) {
        this.exportedAt = exportedAt;
    }

    public UserProfileResponse getProfil() {
        return profil;
    }

    public void setProfil(UserProfileResponse profil) {
        this.profil = profil;
    }

    public List<RendezVousResponse> getRendezVous() {
        return rendezVous;
    }

    public void setRendezVous(List<RendezVousResponse> rendezVous) {
        this.rendezVous = rendezVous;
    }
}
