package com.erdv.service;

import com.erdv.dto.PrestationRequest;
import com.erdv.dto.PlageRecurrenteRequest;
import com.erdv.entity.Prestataire;
import com.erdv.entity.Prestation;
import com.erdv.entity.Utilisateur;
import com.erdv.exception.ApiException;
import com.erdv.repository.PlageRecurrenteRepository;
import com.erdv.repository.PrestationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrestationServiceTest {

    @InjectMocks
    private PrestationService prestationService;

    @Mock
    private PrestationRepository prestationRepository;

    @Mock
    private PrestataireService prestataireService;

    @Mock
    private PrestataireAccessService prestataireAccessService;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listActivesByPrestataire() {
        Prestation p = prestation(1L);
        when(prestataireService.getPrestataireEntityById(2L)).thenReturn(prestataire(2L));
        when(prestationRepository.findByPrestataireIdAndActifTrueOrderByNomAsc(2L)).thenReturn(List.of(p));

        assertEquals(1, prestationService.listActivesByPrestataire(2L).size());
    }

    @Test
    void creerPrestationPourAdmin() {
        loginAdmin();
        when(prestataireAccessService.currentUser()).thenReturn(admin());
        when(prestataireAccessService.resolvePrestataireIdForWrite(any(), org.mockito.ArgumentMatchers.eq(3L)))
                .thenReturn(3L);
        when(prestataireService.getPrestataireEntityById(3L)).thenReturn(prestataire(3L));
        when(prestationRepository.save(any())).thenAnswer(inv -> {
            Prestation saved = inv.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        PrestationRequest req = new PrestationRequest();
        req.setPrestataireId(3L);
        req.setNom("Consultation");
        req.setDureeMinutes(30);
        req.setPrix(BigDecimal.TEN);

        assertEquals("Consultation", prestationService.creer(req).getNom());
    }

    @Test
    void supprimerPrestationInexistante() {
        when(prestationRepository.existsById(99L)).thenReturn(false);
        assertThrows(ApiException.class, () -> prestationService.supprimer(99L));
    }

    @Test
    void getEntityByIdNotFound() {
        when(prestationRepository.findById(1L)).thenReturn(Optional.empty());
        ApiException ex = assertThrows(ApiException.class, () -> prestationService.getEntityById(1L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    private static void loginAdmin() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(admin(), null, admin().getAuthorities()));
    }

    private static Utilisateur admin() {
        Utilisateur u = new Utilisateur();
        u.setRole(Utilisateur.Role.ADMIN);
        u.setEmail("admin@erdv.com");
        return u;
    }

    private static Prestataire prestataire(long id) {
        Prestataire p = new Prestataire();
        p.setId(id);
        p.setNom("Dr");
        p.setSpecialite("Med");
        p.setEmail("dr@test.com");
        return p;
    }

    private static Prestation prestation(long id) {
        Prestation p = new Prestation();
        p.setId(id);
        p.setNom("Consultation");
        p.setDureeMinutes(30);
        p.setActif(true);
        return p;
    }
}
