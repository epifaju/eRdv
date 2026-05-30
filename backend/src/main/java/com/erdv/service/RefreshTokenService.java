package com.erdv.service;

import com.erdv.entity.RefreshToken;
import com.erdv.entity.Utilisateur;
import com.erdv.repository.RefreshTokenRepository;
import com.erdv.repository.UtilisateurRepository;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Transactional
    public String issueRefreshToken(UserDetails userDetails) {
        Long userId = ((Utilisateur) userDetails).getId();
        Utilisateur utilisateur = utilisateurRepository.getReferenceById(userId);
        String jti = UUID.randomUUID().toString();
        Date expiration = new Date(System.currentTimeMillis() + jwtService.getRefreshExpirationMillis());

        RefreshToken entity = new RefreshToken();
        entity.setJti(jti);
        entity.setUtilisateur(utilisateur);
        entity.setExpiresAt(expiration.toInstant());
        refreshTokenRepository.save(entity);

        return jwtService.buildRefreshToken(userDetails.getUsername(), jti, expiration);
    }

    @Transactional
    public String rotateRefreshToken(String currentToken, UserDetails userDetails) {
        revokeRefreshToken(currentToken);
        return issueRefreshToken(userDetails);
    }

    public boolean isRefreshTokenActive(String token, UserDetails userDetails) {
        if (!jwtService.isRefreshToken(token)) {
            return false;
        }
        try {
            String username = jwtService.extractUsername(token);
            if (!username.equals(userDetails.getUsername()) || jwtService.isTokenExpired(token)) {
                return false;
            }
            String jti = jwtService.extractJti(token);
            return refreshTokenRepository.findByJti(jti)
                    .filter(rt -> !rt.isRevoked())
                    .filter(rt -> rt.getExpiresAt().isAfter(Instant.now()))
                    .isPresent();
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    @Transactional
    public void revokeRefreshToken(String token) {
        try {
            if (jwtService.isRefreshToken(token)) {
                refreshTokenRepository.revokeByJti(jwtService.extractJti(token));
            }
        } catch (JwtException | IllegalArgumentException ignored) {
            // Jeton mal formé : rien à révoquer côté serveur
        }
    }

    @Transactional
    public void revokeAllForUser(Long userId) {
        refreshTokenRepository.revokeAllActiveForUser(userId);
    }

    @Transactional
    public void purgeExpired() {
        refreshTokenRepository.deleteExpiredOrRevoked(Instant.now());
    }
}
