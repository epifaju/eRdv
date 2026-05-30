package com.erdv;

import com.erdv.entity.RefreshToken;
import com.erdv.entity.Utilisateur;
import com.erdv.repository.RefreshTokenRepository;
import com.erdv.repository.UtilisateurRepository;
import com.erdv.service.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RefreshTokenServiceTest {

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Utilisateur utilisateur;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        utilisateurRepository.deleteAll();

        utilisateur = new Utilisateur();
        utilisateur.setNom("Purge Test");
        utilisateur.setEmail("purge@test.com");
        utilisateur.setTelephone("0600000000");
        utilisateur.setMotDePasse(passwordEncoder.encode("secret12"));
        utilisateur.setRole(Utilisateur.Role.USER);
        utilisateur = utilisateurRepository.save(utilisateur);
    }

    @Test
    void purgeExpiredRemovesExpiredAndRevokedTokensOnly() {
        saveToken("expired-jti", Instant.now().minus(1, ChronoUnit.HOURS), false);
        saveToken("revoked-jti", Instant.now().plus(7, ChronoUnit.DAYS), true);
        saveToken("active-jti", Instant.now().plus(7, ChronoUnit.DAYS), false);

        int deleted = refreshTokenService.purgeExpired();

        assertEquals(2, deleted);
        assertEquals(1, refreshTokenRepository.count());
        assertTrue(refreshTokenRepository.findByJti("active-jti").isPresent());
    }

    private void saveToken(String jti, Instant expiresAt, boolean revoked) {
        RefreshToken token = new RefreshToken();
        token.setJti(jti);
        token.setUtilisateur(utilisateur);
        token.setExpiresAt(expiresAt);
        token.setRevoked(revoked);
        refreshTokenRepository.save(token);
    }
}
