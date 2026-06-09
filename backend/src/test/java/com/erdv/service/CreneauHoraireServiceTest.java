package com.erdv.service;

import com.erdv.entity.CreneauHoraire;
import com.erdv.entity.Prestataire;
import com.erdv.entity.RendezVous;
import com.erdv.exception.ApiException;
import com.erdv.repository.CreneauHoraireRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreneauHoraireServiceTest {

    @InjectMocks
    private CreneauHoraireService service;

    @Mock
    private CreneauHoraireRepository creneauHoraireRepository;

    @Mock
    private PrestataireService prestataireService;

    @Test
    void computeSlotsNeededArronditAuSuperieur() {
        assertEquals(1, service.computeSlotsNeeded(30, 30));
        assertEquals(2, service.computeSlotsNeeded(60, 30));
        assertEquals(2, service.computeSlotsNeeded(45, 30));
        assertEquals(1, service.computeSlotsNeeded(10, 0));
    }

    @Test
    void reserverCreneauMarqueIndisponible() {
        CreneauHoraire c = creneau(1L, true);
        when(creneauHoraireRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(c));
        when(creneauHoraireRepository.save(c)).thenReturn(c);

        CreneauHoraire reserved = service.reserverCreneau(1L);
        assertTrue(!reserved.isDisponible());
    }

    @Test
    void reserverCreneauIndisponibleLanceConflict() {
        CreneauHoraire c = creneau(1L, false);
        when(creneauHoraireRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(c));

        ApiException ex = assertThrows(ApiException.class, () -> service.reserverCreneau(1L));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test
    void assertCreneauxDisponiblesConsecutifsUnSeulSlot() {
        CreneauHoraire c = creneau(1L, true);
        when(creneauHoraireRepository.findById(1L)).thenReturn(Optional.of(c));

        service.assertCreneauxDisponiblesConsecutifs(1L, 1);
    }

    @Test
    void getCreneauxDisponiblesForDateFiltrePrestationLongue() {
        Prestataire p = new Prestataire();
        p.setId(1L);
        LocalDate date = LocalDate.now().plusDays(1);
        LocalDateTime t1 = date.atTime(9, 0);
        LocalDateTime t2 = date.atTime(9, 30);
        CreneauHoraire c1 = slot(p, t1, 30);
        CreneauHoraire c2 = slot(p, t2, 30);
        when(creneauHoraireRepository.findCreneauxDisponiblesBetween(1L, date.atStartOfDay(), date.plusDays(1).atStartOfDay()))
                .thenReturn(List.of(c1, c2));

        assertEquals(1, service.getCreneauxDisponiblesForDate(1L, date, 60).size());
    }

    @Test
    void marquerCreneauDisponible() {
        CreneauHoraire c = creneau(3L, false);
        when(creneauHoraireRepository.findById(3L)).thenReturn(Optional.of(c));
        when(creneauHoraireRepository.save(c)).thenReturn(c);

        service.marquerCreneauDisponible(3L);
        assertTrue(c.isDisponible());
        verify(creneauHoraireRepository).save(c);
    }

    @Test
    void libererCreneauxRendezVousSimple() {
        CreneauHoraire c = creneau(4L, false);
        RendezVous rdv = new RendezVous();
        rdv.setCreneau(c);
        when(creneauHoraireRepository.findById(4L)).thenReturn(Optional.of(c));
        when(creneauHoraireRepository.save(c)).thenReturn(c);

        service.libererCreneauxRendezVous(rdv);
        assertTrue(c.isDisponible());
    }

    private static CreneauHoraire creneau(long id, boolean disponible) {
        CreneauHoraire c = new CreneauHoraire();
        c.setId(id);
        c.setDisponible(disponible);
        c.setDureeMinutes(30);
        Prestataire p = new Prestataire();
        p.setId(1L);
        c.setPrestataire(p);
        c.setDateHeure(LocalDateTime.now().plusDays(1));
        return c;
    }

    private static CreneauHoraire slot(Prestataire p, LocalDateTime start, int duree) {
        CreneauHoraire c = new CreneauHoraire();
        c.setPrestataire(p);
        c.setDateHeure(start);
        c.setDureeMinutes(duree);
        c.setDisponible(true);
        return c;
    }
}
