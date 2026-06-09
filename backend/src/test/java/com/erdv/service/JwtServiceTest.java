package com.erdv.service;

import com.erdv.entity.Utilisateur;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = "test-jwt-secret-key-at-least-32-characters-long";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expiration", 3_600_000L);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", 86_400_000L);
    }

    @Test
    void generateAccessTokenEtValidation() {
        Utilisateur user = user("client@test.com");
        String token = jwtService.generateAccessToken(user);

        assertTrue(jwtService.isAccessToken(token));
        assertFalse(jwtService.isRefreshToken(token));
        assertEquals("client@test.com", jwtService.extractUsername(token));
        assertTrue(jwtService.validateToken(token, user));
    }

    @Test
    void generateRefreshTokenEtValidation() {
        Utilisateur user = user("client@test.com");
        String token = jwtService.generateRefreshToken(user);

        assertTrue(jwtService.isRefreshToken(token));
        assertFalse(jwtService.isAccessToken(token));
        assertTrue(jwtService.validateRefreshToken(token, user));
        assertFalse(jwtService.validateToken(token, user));
    }

    @Test
    void buildRefreshTokenContientJti() {
        String token = jwtService.buildRefreshToken(
                "user@test.com", "jti-123", new java.util.Date(System.currentTimeMillis() + 86_400_000L));
        assertEquals("jti-123", jwtService.extractJti(token));
        assertNotNull(jwtService.extractUsername(token));
    }

    private static Utilisateur user(String email) {
        Utilisateur u = new Utilisateur();
        u.setEmail(email);
        u.setNom("Test");
        u.setTelephone("0600000000");
        u.setMotDePasse("encoded");
        return u;
    }
}
