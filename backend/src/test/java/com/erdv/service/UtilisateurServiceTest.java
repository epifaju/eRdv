package com.erdv.service;

import com.erdv.dto.ChangePasswordRequest;
import com.erdv.dto.DeleteAccountRequest;
import com.erdv.dto.UpdateProfileRequest;
import com.erdv.entity.Utilisateur;
import com.erdv.exception.ApiException;
import com.erdv.repository.RendezVousRepository;
import com.erdv.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UtilisateurServiceTest {

    @InjectMocks
    private UtilisateurService service;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RendezVousRepository rendezVousRepository;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Test
    void creerUtilisateurRefuseEmailDuplique() {
        when(utilisateurRepository.existsByEmail("dup@test.com")).thenReturn(true);
        Utilisateur u = user(1L, "dup@test.com");

        ApiException ex = assertThrows(ApiException.class, () -> service.creerUtilisateur(u));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test
    void creerUtilisateurEncodeMotDePasse() {
        when(utilisateurRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("plain")).thenReturn("encoded");
        when(utilisateurRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Utilisateur u = user(null, "new@test.com");
        u.setMotDePasse("plain");
        Utilisateur saved = service.creerUtilisateur(u);

        assertEquals("encoded", saved.getMotDePasse());
    }

    @Test
    void updateProfileActiveConsentementSms() {
        Utilisateur u = user(1L, "user@test.com");
        u.setTelephone("0600000000");
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(u));
        when(utilisateurRepository.save(u)).thenReturn(u);

        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setNom("User");
        req.setEmail("user@test.com");
        req.setTelephone("06 12 34 56 78");
        req.setConsentementSmsRappels(true);

        var profile = service.updateProfile(1L, req);
        assertTrue(profile.isConsentementSmsRappels());
        assertTrue(u.getConsentementSmsRappelsAt() != null);
    }

    @Test
    void updateProfileRefuseSmsSansMobileValide() {
        Utilisateur u = user(1L, "user@test.com");
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(u));

        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setNom("User");
        req.setEmail("user@test.com");
        req.setTelephone("invalid");
        req.setConsentementSmsRappels(true);

        ApiException ex = assertThrows(ApiException.class, () -> service.updateProfile(1L, req));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void changePasswordRefuseMotDePasseActuelIncorrect() {
        Utilisateur u = user(1L, "user@test.com");
        u.setMotDePasse("hash");
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setMotDePasseActuel("wrong");
        req.setNouveauMotDePasse("newpass");

        assertThrows(ApiException.class, () -> service.changePassword(1L, req));
    }

    @Test
    void deleteMyAccountAnonymiseEtRevoqueTokens() {
        Utilisateur u = user(1L, "user@test.com");
        u.setMotDePasse("hash");
        u.setRole(Utilisateur.Role.USER);
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("secret", "hash")).thenReturn(true);
        when(passwordEncoder.encode(any())).thenReturn("random");
        when(utilisateurRepository.save(u)).thenReturn(u);

        DeleteAccountRequest req = new DeleteAccountRequest();
        req.setMotDePasse("secret");
        service.deleteMyAccount(1L, req);

        assertFalse(u.isActif());
        assertTrue(u.getEmail().contains("deleted-1@"));
        verify(refreshTokenService).revokeAllForUser(1L);
    }

    @Test
    void deleteMyAccountRefuseAdmin() {
        Utilisateur u = user(1L, "admin@erdv.com");
        u.setRole(Utilisateur.Role.ADMIN);
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(u));

        DeleteAccountRequest req = new DeleteAccountRequest();
        req.setMotDePasse("secret");

        ApiException ex = assertThrows(ApiException.class, () -> service.deleteMyAccount(1L, req));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    private static Utilisateur user(Long id, String email) {
        Utilisateur u = new Utilisateur();
        u.setId(id);
        u.setEmail(email);
        u.setNom("Test");
        u.setTelephone("0600000000");
        u.setMotDePasse("hash");
        u.setRole(Utilisateur.Role.USER);
        return u;
    }
}
