package com.erdv.service;

import com.erdv.dto.RendezVousResponse;
import com.erdv.entity.RendezVous;
import com.erdv.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RendezVousPolicyServiceTest {

    private RendezVousPolicyService service;

    @BeforeEach
    void setUp() {
        service = new RendezVousPolicyService();
        ReflectionTestUtils.setField(service, "delaiAnnulationHeures", 24);
    }

    @Test
    void peutAnnulerParClientSiPlusDe24h() {
        RendezVous rdv = rdv(RendezVous.Statut.CONFIRME, LocalDateTime.now().plusDays(2));
        assertTrue(service.peutAnnulerParClient(rdv));
    }

    @Test
    void refuseAnnulationSiMoinsDe24h() {
        RendezVous rdv = rdv(RendezVous.Statut.CONFIRME, LocalDateTime.now().plusHours(12));
        assertFalse(service.peutAnnulerParClient(rdv));
    }

    @Test
    void refuseAnnulationSiDejaAnnule() {
        RendezVous rdv = rdv(RendezVous.Statut.ANNULE, LocalDateTime.now().plusDays(2));
        assertFalse(service.peutAnnulerParClient(rdv));
    }

    @Test
    void assertClientPeutAnnulerLanceConflictSiDelaiDepasse() {
        RendezVous rdv = rdv(RendezVous.Statut.CONFIRME, LocalDateTime.now().plusHours(6));
        ApiException ex = assertThrows(ApiException.class, () -> service.assertClientPeutAnnuler(rdv));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test
    void enrichResponseRenseigneFlags() {
        RendezVous rdv = rdv(RendezVous.Statut.CONFIRME, LocalDateTime.now().plusDays(3));
        RendezVousResponse response = new RendezVousResponse();
        service.enrichResponse(response, rdv);
        assertEquals(24, response.getDelaiAnnulationHeures());
        assertTrue(Boolean.TRUE.equals(response.getAnnulableParClient()));
    }

    private static RendezVous rdv(RendezVous.Statut statut, LocalDateTime dateHeure) {
        RendezVous rdv = new RendezVous();
        rdv.setStatut(statut);
        rdv.setDateHeure(dateHeure);
        return rdv;
    }
}
