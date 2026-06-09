package com.erdv.service;

import com.erdv.config.SmsProperties;
import com.erdv.entity.Prestataire;
import com.erdv.entity.RendezVous;
import com.erdv.entity.Utilisateur;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

class SmsServiceUnitTest {

    @Nested
    class NormalizePhone {
        @Test
        void frenchMobile() {
            assertEquals("+33612345678", SmsService.normalizePhone("06 12 34 56 78"));
        }

        @Test
        void alreadyE164() {
            assertEquals("+33612345678", SmsService.normalizePhone("+33612345678"));
        }

        @Test
        void invalidReturnsNull() {
            assertNull(SmsService.normalizePhone(""));
            assertNull(SmsService.normalizePhone("abc"));
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class Reminders {
        @InjectMocks
        private SmsService smsService;

        @Mock
        private SmsProperties smsProperties;

        @BeforeEach
        void setUp() {
            ReflectionTestUtils.setField(smsService, "frontendBaseUrl", "http://localhost:3001");
        }

        @Test
        void envoyerRappelJ1SansConsentRetourneFalse() {
            when(smsProperties.isConfigured()).thenReturn(true);
            RendezVous rdv = rdv(false, "0612345678");
            assertFalse(smsService.envoyerRappelJ1(rdv));
        }

        @Test
        void envoyerRappelJ1SansTelephoneRetourneFalse() {
            when(smsProperties.isConfigured()).thenReturn(true);
            RendezVous rdv = rdv(true, null);
            assertFalse(smsService.envoyerRappelJ1(rdv));
        }

        @Test
        void isConfiguredDelegueAuxProperties() {
            when(smsProperties.isConfigured()).thenReturn(true);
            assertEquals(true, smsService.isConfigured());
        }

        private static RendezVous rdv(boolean consent, String phone) {
            Utilisateur u = new Utilisateur();
            u.setTelephone(phone);
            u.setConsentementSmsRappels(consent);
            Prestataire p = new Prestataire();
            p.setNom("Dr Test");
            RendezVous rdv = new RendezVous();
            rdv.setId(1L);
            rdv.setUtilisateur(u);
            rdv.setPrestataire(p);
            rdv.setService("Consultation");
            rdv.setDateHeure(LocalDateTime.now().plusDays(1));
            rdv.setStatut(RendezVous.Statut.CONFIRME);
            return rdv;
        }
    }
}
