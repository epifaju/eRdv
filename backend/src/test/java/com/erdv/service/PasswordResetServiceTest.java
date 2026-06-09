package com.erdv.service;

import com.erdv.entity.PasswordResetToken;
import com.erdv.entity.Utilisateur;
import com.erdv.exception.ApiException;
import com.erdv.repository.PasswordResetTokenRepository;
import com.erdv.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @InjectMocks
    private PasswordResetService service;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Test
    void requestPasswordResetInconnuNeFaitRien() {
        when(utilisateurRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        service.requestPasswordReset("unknown@test.com");

        verify(passwordResetTokenRepository, never()).save(any());
    }

    @Test
    void requestPasswordResetCreeTokenEtEnvoieEmail() {
        Utilisateur u = user(1L);
        when(utilisateurRepository.findByEmail("user@test.com")).thenReturn(Optional.of(u));
        ReflectionTestUtils.setField(service, "frontendBaseUrl", "http://localhost:3001");

        service.requestPasswordReset("user@test.com");

        verify(passwordResetTokenRepository).deleteByUtilisateurId(1L);
        verify(passwordResetTokenRepository).save(any());
        verify(emailService).envoyerLienReinitialisationMotDePasse(
                org.mockito.ArgumentMatchers.eq("user@test.com"),
                org.mockito.ArgumentMatchers.contains("/reset-password?token="));
    }

    @Test
    void resetPasswordRefuseTokenExpire() {
        PasswordResetToken prt = token("abc", false, Instant.now().minus(1, ChronoUnit.HOURS));
        when(passwordResetTokenRepository.findByToken("abc")).thenReturn(Optional.of(prt));

        ApiException ex = assertThrows(ApiException.class, () -> service.resetPassword("abc", "newpass"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void resetPasswordMetAJourMotDePasse() {
        Utilisateur u = user(2L);
        PasswordResetToken prt = token("valid", false, Instant.now().plus(30, ChronoUnit.MINUTES));
        prt.setUtilisateur(u);
        when(passwordResetTokenRepository.findByToken("valid")).thenReturn(Optional.of(prt));
        when(passwordEncoder.encode("newpass")).thenReturn("encoded");
        when(utilisateurRepository.save(u)).thenReturn(u);
        when(passwordResetTokenRepository.save(prt)).thenReturn(prt);

        service.resetPassword("valid", "newpass");

        assertEquals("encoded", u.getMotDePasse());
        assertEquals(true, prt.isUsed());
    }

    private static Utilisateur user(long id) {
        Utilisateur u = new Utilisateur();
        u.setId(id);
        u.setEmail("user@test.com");
        return u;
    }

    private static PasswordResetToken token(String value, boolean used, Instant expiresAt) {
        PasswordResetToken prt = new PasswordResetToken();
        prt.setToken(value);
        prt.setUsed(used);
        prt.setExpiresAt(expiresAt);
        return prt;
    }
}
