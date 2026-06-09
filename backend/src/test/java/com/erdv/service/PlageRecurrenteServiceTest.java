package com.erdv.service;

import com.erdv.dto.GenerationCreneauxResponse;
import com.erdv.dto.PlageRecurrenteRequest;
import com.erdv.entity.PlageRecurrente;
import com.erdv.entity.Prestataire;
import com.erdv.entity.Utilisateur;
import com.erdv.exception.ApiException;
import com.erdv.repository.PlageRecurrenteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlageRecurrenteServiceTest {

    @InjectMocks
    private PlageRecurrenteService plageRecurrenteService;

    @Mock
    private PlageRecurrenteRepository plageRecurrenteRepository;

    @Mock
    private PrestataireService prestataireService;

    @Mock
    private CreneauGenerationService creneauGenerationService;

    @Mock
    private PrestataireAccessService prestataireAccessService;

    @Test
    void creerRefuseHeureFinAvantDebut() {
        PlageRecurrenteRequest req = new PlageRecurrenteRequest();
        req.setHeureDebut(LocalTime.of(17, 0));
        req.setHeureFin(LocalTime.of(9, 0));

        ApiException ex = assertThrows(ApiException.class, () -> plageRecurrenteService.creer(req));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void genererCreneauxDelegueAuServiceGeneration() {
        GenerationCreneauxResponse expected = new GenerationCreneauxResponse(5, 30, 7);
        when(creneauGenerationService.genererPourPrestataire(2L, 7)).thenReturn(expected);

        assertEquals(5, plageRecurrenteService.genererCreneaux(2L, 7).getCreneauxCrees());
        verify(prestataireAccessService).assertCanManagePrestataire(2L);
    }

    @Test
    void supprimerPlageInexistante() {
        when(plageRecurrenteRepository.existsById(8L)).thenReturn(false);
        assertThrows(ApiException.class, () -> plageRecurrenteService.supprimer(8L));
    }

    @Test
    void modifierPlageIntrouvable() {
        PlageRecurrenteRequest req = validRequest();
        when(plageRecurrenteRepository.findById(1L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> plageRecurrenteService.modifier(1L, req));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void creerPlagePourPrestataire() {
        Utilisateur admin = new Utilisateur();
        admin.setRole(Utilisateur.Role.ADMIN);
        when(prestataireAccessService.currentUser()).thenReturn(admin);
        when(prestataireAccessService.resolvePrestataireIdForWrite(admin, 1L)).thenReturn(1L);
        when(prestataireService.getPrestataireEntityById(1L)).thenReturn(prestataire(1L));
        when(plageRecurrenteRepository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(inv -> {
            PlageRecurrente plage = inv.getArgument(0);
            plage.setId(5L);
            return plage;
        });

        assertEquals(1, plageRecurrenteService.creer(validRequest()).getJourSemaine());
    }

    private static PlageRecurrenteRequest validRequest() {
        PlageRecurrenteRequest req = new PlageRecurrenteRequest();
        req.setPrestataireId(1L);
        req.setJourSemaine(1);
        req.setHeureDebut(LocalTime.of(9, 0));
        req.setHeureFin(LocalTime.of(12, 0));
        return req;
    }

    private static Prestataire prestataire(long id) {
        Prestataire p = new Prestataire();
        p.setId(id);
        p.setNom("Dr");
        p.setSpecialite("Med");
        p.setEmail("dr@test.com");
        return p;
    }
}
