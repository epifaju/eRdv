package com.erdv.controller;

import com.erdv.dto.CompletePaymentRequest;
import com.erdv.dto.CreatePaymentIntentRequest;
import com.erdv.dto.PaymentConfigResponse;
import com.erdv.dto.PaymentIntentResponse;
import com.erdv.dto.RendezVousResponse;
import com.erdv.entity.Utilisateur;
import com.erdv.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/config")
    public ResponseEntity<PaymentConfigResponse> getConfig() {
        return ResponseEntity.ok(paymentService.getConfig());
    }

    @PostMapping("/intent")
    public ResponseEntity<PaymentIntentResponse> createIntent(
            @Valid @RequestBody CreatePaymentIntentRequest request) {
        return ResponseEntity.ok(paymentService.createIntent(currentUser(), request));
    }

    @PostMapping("/complete")
    public ResponseEntity<RendezVousResponse> completePayment(
            @Valid @RequestBody CompletePaymentRequest request) {
        return ResponseEntity.ok(paymentService.completePayment(currentUser(), request));
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String signature) {
        paymentService.handleWebhook(payload, signature);
        return ResponseEntity.ok("ok");
    }

    private static Utilisateur currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Utilisateur) auth.getPrincipal();
    }
}
