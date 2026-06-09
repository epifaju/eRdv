package com.erdv.service;

import com.erdv.entity.RefreshToken;
import com.erdv.entity.Utilisateur;
import com.erdv.repository.RefreshTokenRepository;
import com.erdv.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceUnitTest {

    @InjectMocks
    private RefreshTokenService service;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Test
    void issueRefreshTokenPersisteEtRetourneJwt() {
        Utilisateur user = user(1L, "user@test.com");
        when(jwtService.getRefreshExpirationMillis()).thenReturn(86_400_000L);
        when(utilisateurRepository.getReferenceById(1L)).thenReturn(user);
        when(jwtService.buildRefreshToken(org.mockito.ArgumentMatchers.eq("user@test.com"),
                any(), any(Date.class))).thenReturn("refresh-jwt");

        String token = service.issueRefreshToken(user);

        assertEquals("refresh-jwt", token);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void isRefreshTokenActiveRefuseTokenRevoque() {
        Utilisateur user = user(1L, "user@test.com");
        when(jwtService.isRefreshToken("token")).thenReturn(true);
        when(jwtService.extractUsername("token")).thenReturn("user@test.com");
        when(jwtService.isTokenExpired("token")).thenReturn(false);
        when(jwtService.extractJti("token")).thenReturn("jti-1");

        RefreshToken rt = new RefreshToken();
        rt.setRevoked(true);
        rt.setExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));
        when(refreshTokenRepository.findByJti("jti-1")).thenReturn(Optional.of(rt));

        assertFalse(service.isRefreshTokenActive("token", user));
    }

    @Test
    void isRefreshTokenActiveAccepteTokenValide() {
        Utilisateur user = user(1L, "user@test.com");
        when(jwtService.isRefreshToken("token")).thenReturn(true);
        when(jwtService.extractUsername("token")).thenReturn("user@test.com");
        when(jwtService.isTokenExpired("token")).thenReturn(false);
        when(jwtService.extractJti("token")).thenReturn("jti-2");

        RefreshToken rt = new RefreshToken();
        rt.setRevoked(false);
        rt.setExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));
        when(refreshTokenRepository.findByJti("jti-2")).thenReturn(Optional.of(rt));

        assertTrue(service.isRefreshTokenActive("token", user));
    }

    @Test
    void purgeExpiredDelegueAuRepository() {
        when(refreshTokenRepository.deleteExpiredOrRevoked(any())).thenReturn(3);
        assertEquals(3, service.purgeExpired());
    }

    @Test
    void revokeAllForUser() {
        service.revokeAllForUser(5L);
        verify(refreshTokenRepository).revokeAllActiveForUser(5L);
    }

    private static Utilisateur user(long id, String email) {
        Utilisateur u = new Utilisateur();
        u.setId(id);
        u.setEmail(email);
        u.setNom("Test");
        u.setTelephone("0600000000");
        u.setMotDePasse("hash");
        return u;
    }
}
