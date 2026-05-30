package com.erdv.dto;

import jakarta.validation.constraints.NotBlank;

public class CompletePaymentRequest {

    @NotBlank
    private String paymentIntentId;

    public String getPaymentIntentId() {
        return paymentIntentId;
    }

    public void setPaymentIntentId(String paymentIntentId) {
        this.paymentIntentId = paymentIntentId;
    }
}
