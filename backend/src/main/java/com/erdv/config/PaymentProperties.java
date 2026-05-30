package com.erdv.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.payment")
public class PaymentProperties {

    private boolean enabled = false;
    private String stripeSecretKey = "";
    private String stripePublishableKey = "";
    private String stripeWebhookSecret = "";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getStripeSecretKey() {
        return stripeSecretKey;
    }

    public void setStripeSecretKey(String stripeSecretKey) {
        this.stripeSecretKey = stripeSecretKey;
    }

    public String getStripePublishableKey() {
        return stripePublishableKey;
    }

    public void setStripePublishableKey(String stripePublishableKey) {
        this.stripePublishableKey = stripePublishableKey;
    }

    public String getStripeWebhookSecret() {
        return stripeWebhookSecret;
    }

    public void setStripeWebhookSecret(String stripeWebhookSecret) {
        this.stripeWebhookSecret = stripeWebhookSecret;
    }

    public boolean isConfigured() {
        return enabled
                && stripeSecretKey != null && !stripeSecretKey.isBlank()
                && stripePublishableKey != null && !stripePublishableKey.isBlank();
    }
}
