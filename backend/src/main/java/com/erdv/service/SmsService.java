package com.erdv.service;

import com.erdv.config.SmsProperties;
import com.erdv.entity.RendezVous;
import com.erdv.exception.ApiException;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsService.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM 'à' HH:mm");

    @Autowired
    private SmsProperties smsProperties;

    @Value("${app.frontend.base-url:http://localhost:3001}")
    private String frontendBaseUrl;

    @PostConstruct
    void initTwilio() {
        if (smsProperties.isConfigured()) {
            Twilio.init(smsProperties.getTwilioAccountSid(), smsProperties.getTwilioAuthToken());
        }
    }

    public boolean isConfigured() {
        return smsProperties.isConfigured();
    }

    public boolean envoyerRappelJ1(RendezVous rendezVous) {
        String phone = resolvePhone(rendezVous);
        if (phone == null) {
            log.debug("SMS J-1 ignoré (téléphone absent ou invalide) pour RDV {}", rendezVous.getId());
            return false;
        }
        String when = rendezVous.getDateHeure().format(DATE_FORMAT);
        String prestataire = rendezVous.getPrestataire().getNom();
        String service = rendezVous.getService();
        String statut = rendezVous.getStatut() == RendezVous.Statut.CONFIRME ? "confirmé" : "en attente";
        String body = String.format(
                "eRDV: RDV demain %s (%s) — %s, %s. %s",
                when, statut, prestataire, service, mesRendezVousUrl());
        sendSms(phone, body);
        return true;
    }

    public boolean envoyerRappelH2(RendezVous rendezVous) {
        String phone = resolvePhone(rendezVous);
        if (phone == null) {
            log.debug("SMS H-2 ignoré (téléphone absent ou invalide) pour RDV {}", rendezVous.getId());
            return false;
        }
        String when = rendezVous.getDateHeure().format(DATE_FORMAT);
        String prestataire = rendezVous.getPrestataire().getNom();
        String body = String.format(
                "eRDV: RDV dans ~2 h (%s) — %s. %s",
                when, prestataire, mesRendezVousUrl());
        sendSms(phone, body);
        return true;
    }

    private String resolvePhone(RendezVous rendezVous) {
        if (!smsProperties.isConfigured()) {
            return null;
        }
        if (rendezVous.getUtilisateur() == null) {
            return null;
        }
        if (!rendezVous.getUtilisateur().isConsentementSmsRappels()) {
            return null;
        }
        return normalizePhone(rendezVous.getUtilisateur().getTelephone());
    }

    public static String normalizePhone(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String digits = raw.replaceAll("[^0-9+]", "");
        if (digits.startsWith("00")) {
            digits = "+" + digits.substring(2);
        }
        if (digits.startsWith("+")) {
            return digits.length() >= 10 ? digits : null;
        }
        if (digits.startsWith("0") && digits.length() == 10) {
            return "+33" + digits.substring(1);
        }
        if (digits.length() == 9) {
            return "+33" + digits;
        }
        return null;
    }

    private void sendSms(String toE164, String body) {
        try {
            Message message = Message.creator(
                    new PhoneNumber(toE164),
                    new PhoneNumber(smsProperties.getTwilioFromNumber()),
                    body).create();
            log.info("SMS Twilio envoyé vers {} (sid={})", toE164, message.getSid());
        } catch (Exception e) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Envoi SMS impossible : " + e.getMessage());
        }
    }

    private String mesRendezVousUrl() {
        return frontendBaseUrl.replaceAll("/+$", "") + "/mes-rendez-vous";
    }
}
