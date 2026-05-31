package com.erdv.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.sms")
public class SmsProperties {

    private boolean enabled = false;
    private String twilioAccountSid = "";
    private String twilioAuthToken = "";
    private String twilioFromNumber = "";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getTwilioAccountSid() {
        return twilioAccountSid;
    }

    public void setTwilioAccountSid(String twilioAccountSid) {
        this.twilioAccountSid = twilioAccountSid;
    }

    public String getTwilioAuthToken() {
        return twilioAuthToken;
    }

    public void setTwilioAuthToken(String twilioAuthToken) {
        this.twilioAuthToken = twilioAuthToken;
    }

    public String getTwilioFromNumber() {
        return twilioFromNumber;
    }

    public void setTwilioFromNumber(String twilioFromNumber) {
        this.twilioFromNumber = twilioFromNumber;
    }

    public boolean isConfigured() {
        return enabled
                && twilioAccountSid != null && !twilioAccountSid.isBlank()
                && twilioAuthToken != null && !twilioAuthToken.isBlank()
                && twilioFromNumber != null && !twilioFromNumber.isBlank();
    }
}
