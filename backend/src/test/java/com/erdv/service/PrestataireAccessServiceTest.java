package com.erdv.service;

import com.erdv.entity.PlageRecurrente;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrestataireAccessServiceTest {

    @InjectMocks
    private PrestataireAccessService service;

    @Mock
    private PrestationRepository prestationRepository;

    @Mock
    private PlageRecurrenteRepository plageRecurrenteRepository;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void adminPeutGererToutPrestataire() {
        Utilisateur admin = user(Utilisateur.Role.ADMIN, null);
        service.assertCanManagePrestataire(admin, 99L);
    }

    @Test
    void prestataireNePeutGererQueSaFiche() {
        Prestataire p = prestataire(1L);
        Utilisateur user = user(Utilisateur.Role.PRESTATAIRE, p);

        service.assertCanManagePrestataire(user, 1L);

        assertThrows(AccessDeniedException.class,
                () -> service.assertCanManagePrestataire(user, 2L));
    }

    @Test
    void resolvePrestataireIdForWritePrestataireForceSonId() {
        Prestataire p = prestataire(5L);
        Utilisateur user = user(Utilisateur.Role.PRESTATAIRE, p);
        assertEquals(5L, service.resolvePrestataireIdForWrite(user, null));
    }

    @Test
    void resolvePrestataireIdForWriteAdminExigeId() {
        Utilisateur admin = user(Utilisateur.Role.ADMIN, null);
        ApiException ex = assertThrows(ApiException.class,
                () -> service.resolvePrestataireIdForWrite(admin, null));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void assertCanManagePrestationViaRepository() {
        Prestataire p = prestataire(1L);
        Prestation prestation = new Prestation();
        prestation.setId(10L);
        prestation.setPrestataire(p);
        when(prestationRepository.findById(10L)).thenReturn(Optional.of(prestation));

        login(user(Utilisateur.Role.ADMIN, null));
        service.assertCanManagePrestation(10L);
    }

    @Test
    void assertCanManagePlageViaRepository() {
        Prestataire p = prestataire(1L);
        PlageRecurrente plage = new PlageRecurrente();
        plage.setId(20L);
        plage.setPrestataire(p);
        when(plageRecurrenteRepository.findById(20L)).thenReturn(Optional.of(plage));

        login(user(Utilisateur.Role.ADMIN, null));
        service.assertCanManagePlage(20L);
    }

    private static void login(Utilisateur user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }

    private static Utilisateur user(Utilisateur.Role role, Prestataire prestataire) {
        Utilisateur u = new Utilisateur();
        u.setRole(role);
        u.setPrestataire(prestataire);
        u.setEmail("test@erdv.com");
        return u;
    }

    private static Prestataire prestataire(long id) {
        Prestataire p = new Prestataire();
        p.setId(id);
        p.setNom("Dr Test");
        p.setSpecialite("Med");
        p.setEmail("dr@test.com");
        return p;
    }
}
