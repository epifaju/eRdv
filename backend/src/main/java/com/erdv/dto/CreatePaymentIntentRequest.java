package com.erdv.dto;

import jakarta.validation.constraints.NotNull;

public class CreatePaymentIntentRequest {

    @NotNull
    private Long creneauId;

    @NotNull
    private Long prestationId;

    private String service;

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
}
