package com.erdv.service;

import com.erdv.entity.PlageRecurrente;
import com.erdv.entity.Prestataire;
import com.erdv.exception.ApiException;
import com.erdv.repository.CreneauHoraireRepository;
import com.erdv.repository.PlageRecurrenteRepository;
import com.erdv.repository.PrestataireRepository;
import com.erdv.repository.PrestationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreneauGenerationServiceTest {

    @InjectMocks
    private CreneauGenerationService service;

    @Mock
    private PrestataireRepository prestataireRepository;

    @Mock
    private PlageRecurrenteRepository plageRecurrenteRepository;

    @Mock
    private PrestationRepository prestationRepository;

    @Mock
    private CreneauHoraireRepository creneauHoraireRepository;

    @Test
    void genererRefuseSansPlageActive() {
        Prestataire p = new Prestataire();
        p.setId(1L);
        when(prestataireRepository.findById(1L)).thenReturn(Optional.of(p));
        when(plageRecurrenteRepository.findByPrestataireIdAndActifTrueOrderByJourSemaineAscHeureDebutAsc(1L))
                .thenReturn(List.of());

        ApiException ex = assertThrows(ApiException.class, () -> service.genererPourPrestataire(1L, 7));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void genererCreeCreneauxPourPlageDuJour() {
        ReflectionTestUtils.setField(service, "horizonJours", 28);

        Prestataire p = new Prestataire();
        p.setId(1L);
        when(prestataireRepository.findById(1L)).thenReturn(Optional.of(p));

        int isoDow = java.time.LocalDate.now().plusDays(1).getDayOfWeek().getValue();
        PlageRecurrente plage = new PlageRecurrente();
        plage.setJourSemaine(isoDow);
        plage.setHeureDebut(LocalTime.of(9, 0));
        plage.setHeureFin(LocalTime.of(10, 0));
        when(plageRecurrenteRepository.findByPrestataireIdAndActifTrueOrderByJourSemaineAscHeureDebutAsc(1L))
                .thenReturn(List.of(plage));
        when(prestationRepository.findMinDureeActive(1L)).thenReturn(Optional.of(30));
        when(creneauHoraireRepository.existsByPrestataireIdAndDateHeure(eq(1L), any())).thenReturn(false);
        when(creneauHoraireRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = service.genererPourPrestataire(1L, 2);
        assertEquals(30, response.getGranulariteMinutes());
        assertTrue(response.getCreneauxCrees() >= 1);
    }
}
