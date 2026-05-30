package com.erdv.service;

import com.erdv.entity.PasswordResetToken;
import com.erdv.entity.Utilisateur;
import com.erdv.exception.ApiException;
import com.erdv.repository.PasswordResetTokenRepository;
import com.erdv.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class PasswordResetService {

    private static final Duration TOKEN_TTL = Duration.ofHours(1);

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Value("${app.frontend.base-url:http://localhost:3001}")
    private String frontendBaseUrl;

    /**
     * Ne révèle pas si l'email existe (réponse identique côté contrôleur).
     */
    public void requestPasswordReset(String email) {
        utilisateurRepository.findByEmail(email).ifPresent(u -> {
            passwordResetTokenRepository.deleteByUtilisateurId(u.getId());
            String token = UUID.randomUUID().toString().replace("-", "");
            PasswordResetToken prt = new PasswordResetToken();
            prt.setToken(token);
            prt.setUtilisateur(u);
            prt.setExpiresAt(Instant.now().plus(TOKEN_TTL));
            prt.setUsed(false);
            passwordResetTokenRepository.save(prt);
            String base = frontendBaseUrl.replaceAll("/+$", "");
            String resetUrl = base + "/reset-password?token=" + token;
            try {
                emailService.envoyerLienReinitialisationMotDePasse(u.getEmail(), resetUrl);
            } catch (Exception e) {
                System.err.println("Mail reset mot de passe: " + e.getMessage());
            }
        });
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken prt = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Lien invalide ou expiré"));
        if (prt.isUsed() || prt.getExpiresAt().isBefore(Instant.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Lien invalide ou expiré");
        }
        Utilisateur u = prt.getUtilisateur();
        u.setMotDePasse(passwordEncoder.encode(newPassword));
        utilisateurRepository.save(u);
        prt.setUsed(true);
        passwordResetTokenRepository.save(prt);
    }
}
