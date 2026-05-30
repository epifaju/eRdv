package com.erdv.service;

import com.erdv.config.PaymentProperties;
import com.erdv.dto.CompletePaymentRequest;
import com.erdv.dto.CreatePaymentIntentRequest;
import com.erdv.dto.CreateRendezVousRequest;
import com.erdv.dto.PaymentConfigResponse;
import com.erdv.dto.PaymentIntentResponse;
import com.erdv.dto.PaymentSummaryResponse;
import com.erdv.dto.RendezVousResponse;
import com.erdv.entity.CreneauHoraire;
import com.erdv.entity.Payment;
import com.erdv.entity.Prestation;
import com.erdv.entity.RendezVous;
import com.erdv.entity.Utilisateur;
import com.erdv.exception.ApiException;
import com.erdv.repository.PaymentRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class PaymentService {

    @Autowired
    private PaymentProperties paymentProperties;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PrestationService prestationService;

    @Autowired
    private CreneauHoraireService creneauHoraireService;

    @Autowired
    @Lazy
    private RendezVousService rendezVousService;

    @PostConstruct
    void initStripe() {
        if (paymentProperties.isConfigured()) {
            com.stripe.Stripe.apiKey = paymentProperties.getStripeSecretKey();
        }
    }

    @Transactional(readOnly = true)
    public PaymentConfigResponse getConfig() {
        if (!paymentProperties.isConfigured()) {
            return new PaymentConfigResponse(false, null);
        }
        return new PaymentConfigResponse(true, paymentProperties.getStripePublishableKey());
    }

    @Transactional(readOnly = true)
    public boolean isPaymentRequired(Prestation prestation) {
        return paymentProperties.isConfigured()
                && prestation != null
                && prestation.getPrix() != null
                && prestation.getPrix().compareTo(BigDecimal.ZERO) > 0;
    }

    public PaymentIntentResponse createIntent(Utilisateur utilisateur, CreatePaymentIntentRequest request) {
        assertPaymentConfigured();

        Prestation prestation = prestationService.getEntityById(request.getPrestationId());
        if (!prestation.isActif()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cette prestation n'est plus disponible");
        }
        if (!isPaymentRequired(prestation)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cette prestation ne nécessite pas de paiement en ligne");
        }

        CreneauHoraire creneauRef = creneauHoraireService.getCreneauById(request.getCreneauId());
        if (!prestation.getPrestataire().getId().equals(creneauRef.getPrestataire().getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cette prestation n'appartient pas au prestataire sélectionné");
        }

        int nbSlots = creneauHoraireService.computeSlotsNeeded(
                prestation.getDureeMinutes(), creneauRef.getDureeMinutes());
        creneauHoraireService.assertCreneauxDisponiblesConsecutifs(request.getCreneauId(), nbSlots);

        long amountCents = toCents(prestation.getPrix());
        Map<String, String> metadata = new HashMap<>();
        metadata.put("userId", utilisateur.getId().toString());
        metadata.put("creneauId", request.getCreneauId().toString());
        metadata.put("prestationId", request.getPrestationId().toString());

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountCents)
                    .setCurrency("eur")
                    .putAllMetadata(metadata)
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build())
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);

            Payment payment = new Payment();
            payment.setUtilisateur(utilisateur);
            payment.setPrestation(prestation);
            payment.setCreneauId(request.getCreneauId());
            payment.setMontant(prestation.getPrix());
            payment.setDevise("EUR");
            payment.setStripePaymentIntentId(intent.getId());
            payment.setStatut(Payment.Statut.PENDING);
            if (request.getService() != null && !request.getService().isBlank()) {
                payment.setServiceNotes(request.getService().trim());
            }
            payment = paymentRepository.save(payment);

            PaymentIntentResponse response = new PaymentIntentResponse();
            response.setClientSecret(intent.getClientSecret());
            response.setPaymentId(payment.getId());
            response.setMontant(payment.getMontant());
            response.setDevise(payment.getDevise());
            return response;
        } catch (StripeException e) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Impossible de créer le paiement : " + e.getMessage());
        }
    }

    public RendezVousResponse completePayment(Utilisateur utilisateur, CompletePaymentRequest request) {
        assertPaymentConfigured();

        Payment payment = paymentRepository.findByStripePaymentIntentId(request.getPaymentIntentId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Paiement introuvable"));

        if (!payment.getUtilisateur().getId().equals(utilisateur.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Paiement non autorisé");
        }

        if (payment.getRendezVous() != null) {
            return rendezVousService.getRendezVousById(payment.getRendezVous().getId(), utilisateur);
        }

        PaymentIntent intent = retrieveIntent(request.getPaymentIntentId());
        if (!"succeeded".equals(intent.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Le paiement n'est pas confirmé");
        }

        return finalizePaidBooking(payment, intent);
    }

    public void handleWebhook(String payload, String signatureHeader) {
        if (!paymentProperties.isConfigured()) {
            return;
        }
        if (paymentProperties.getStripeWebhookSecret() == null
                || paymentProperties.getStripeWebhookSecret().isBlank()) {
            return;
        }

        Event event;
        try {
            event = Webhook.constructEvent(payload, signatureHeader, paymentProperties.getStripeWebhookSecret());
        } catch (SignatureVerificationException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Signature webhook invalide");
        }

        if (!"payment_intent.succeeded".equals(event.getType())) {
            return;
        }

        PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject()
                .filter(PaymentIntent.class::isInstance)
                .map(PaymentIntent.class::cast)
                .orElse(null);
        if (intent == null) {
            return;
        }

        paymentRepository.findByStripePaymentIntentId(intent.getId()).ifPresent(payment -> {
            if (payment.getRendezVous() == null && payment.getStatut() != Payment.Statut.SUCCEEDED) {
                finalizePaidBooking(payment, intent);
            }
        });
    }

    public void refundIfPaid(RendezVous rendezVous) {
        if (!paymentProperties.isConfigured()) {
            return;
        }
        Optional<Payment> opt = paymentRepository.findByRendezVousId(rendezVous.getId());
        if (opt.isEmpty() || opt.get().getStatut() != Payment.Statut.SUCCEEDED) {
            return;
        }
        Payment payment = opt.get();
        try {
            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(payment.getStripePaymentIntentId())
                    .build();
            Refund.create(params);
            payment.setStatut(Payment.Statut.REFUNDED);
            paymentRepository.save(payment);
        } catch (StripeException e) {
            System.err.println("Erreur remboursement Stripe: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public PaymentSummaryResponse getPaymentSummaryForRendezVous(Long rendezVousId) {
        return paymentRepository.findByRendezVousId(rendezVousId)
                .map(PaymentSummaryResponse::from)
                .orElse(null);
    }

    private RendezVousResponse finalizePaidBooking(Payment payment, PaymentIntent intent) {
        if (payment.getRendezVous() != null) {
            payment.setStatut(Payment.Statut.SUCCEEDED);
            paymentRepository.save(payment);
            return rendezVousService.getRendezVousByIdInternal(payment.getRendezVous().getId());
        }

        long expectedCents = toCents(payment.getMontant());
        if (intent.getAmount() == null || intent.getAmount() != expectedCents) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Montant du paiement invalide");
        }

        CreateRendezVousRequest rdvRequest = new CreateRendezVousRequest();
        rdvRequest.setCreneauId(payment.getCreneauId());
        rdvRequest.setPrestationId(payment.getPrestation().getId());
        if (payment.getServiceNotes() != null && !payment.getServiceNotes().isBlank()) {
            rdvRequest.setService(payment.getServiceNotes());
        }
        rdvRequest.setStatut(RendezVous.Statut.CONFIRME);

        RendezVousResponse rdv = rendezVousService.creerRendezVousApresPaiement(payment.getUtilisateur(), rdvRequest);

        payment.setStatut(Payment.Statut.SUCCEEDED);
        payment.setRendezVous(rendezVousService.getEntityById(rdv.getId()));
        paymentRepository.save(payment);

        rdv.setPayment(PaymentSummaryResponse.from(payment));
        return rdv;
    }

    private PaymentIntent retrieveIntent(String paymentIntentId) {
        try {
            return PaymentIntent.retrieve(paymentIntentId);
        } catch (StripeException e) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Impossible de vérifier le paiement");
        }
    }

    private void assertPaymentConfigured() {
        if (!paymentProperties.isConfigured()) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Paiement en ligne non configuré");
        }
    }

    private static long toCents(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }
}
