package com.erdv.dto;

public class PaymentConfigResponse {

    private boolean enabled;
    private String publishableKey;

    public PaymentConfigResponse() {
    }

    public PaymentConfigResponse(boolean enabled, String publishableKey) {
        this.enabled = enabled;
        this.publishableKey = publishableKey;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getPublishableKey() {
        return publishableKey;
    }

    public void setPublishableKey(String publishableKey) {
        this.publishableKey = publishableKey;
    }
}
