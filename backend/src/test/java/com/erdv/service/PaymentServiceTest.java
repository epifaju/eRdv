package com.erdv.service;

import com.erdv.config.PaymentProperties;
import com.erdv.dto.CompletePaymentRequest;
import com.erdv.entity.Payment;
import com.erdv.entity.Prestation;
import com.erdv.entity.Utilisateur;
import com.erdv.exception.ApiException;
import com.erdv.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private PaymentProperties paymentProperties;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PrestationService prestationService;

    @Mock
    private CreneauHoraireService creneauHoraireService;

    @Mock
    private RendezVousService rendezVousService;

    @Test
    void getConfigDesactiveSiNonConfigure() {
        when(paymentProperties.isConfigured()).thenReturn(false);
        assertFalse(paymentService.getConfig().isEnabled());
        assertNull(paymentService.getConfig().getPublishableKey());
    }

    @Test
    void getConfigExposeClePublique() {
        when(paymentProperties.isConfigured()).thenReturn(true);
        when(paymentProperties.getStripePublishableKey()).thenReturn("pk_test_123");
        assertTrue(paymentService.getConfig().isEnabled());
        assertEquals("pk_test_123", paymentService.getConfig().getPublishableKey());
    }

    @Test
    void isPaymentRequiredSiPrestationPayanteEtStripeActif() {
        when(paymentProperties.isConfigured()).thenReturn(true);
        Prestation p = new Prestation();
        p.setPrix(new BigDecimal("25.00"));
        assertTrue(paymentService.isPaymentRequired(p));
    }

    @Test
    void isPaymentRequiredFalseSiGratuit() {
        when(paymentProperties.isConfigured()).thenReturn(true);
        Prestation p = new Prestation();
        p.setPrix(BigDecimal.ZERO);
        assertFalse(paymentService.isPaymentRequired(p));
    }

    @Test
    void createIntentRefuseSiPaiementNonConfigure() {
        when(paymentProperties.isConfigured()).thenReturn(false);
        ApiException ex = assertThrows(ApiException.class,
                () -> paymentService.createIntent(user(1L), new com.erdv.dto.CreatePaymentIntentRequest()));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getStatus());
    }

    @Test
    void completePaymentRefuseSiMauvaisUtilisateur() {
        when(paymentProperties.isConfigured()).thenReturn(true);
        Utilisateur owner = user(1L);
        Utilisateur other = user(2L);
        Payment payment = new Payment();
        payment.setUtilisateur(owner);
        payment.setStripePaymentIntentId("pi_123");
        when(paymentRepository.findByStripePaymentIntentId("pi_123")).thenReturn(Optional.of(payment));

        CompletePaymentRequest req = new CompletePaymentRequest();
        req.setPaymentIntentId("pi_123");

        ApiException ex = assertThrows(ApiException.class, () -> paymentService.completePayment(other, req));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    private static Utilisateur user(long id) {
        Utilisateur u = new Utilisateur();
        u.setId(id);
        u.setEmail("u" + id + "@test.com");
        return u;
    }
}
