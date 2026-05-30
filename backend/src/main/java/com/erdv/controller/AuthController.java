package com.erdv.controller;

import com.erdv.dto.AuthRequest;
import com.erdv.dto.AuthResponse;
import com.erdv.dto.ForgotPasswordRequest;
import com.erdv.dto.RefreshTokenRequest;
import com.erdv.dto.RegisterRequest;
import com.erdv.dto.ResetPasswordRequest;
import com.erdv.entity.Utilisateur;
import com.erdv.exception.ApiException;
import com.erdv.service.JwtService;
import com.erdv.service.PasswordResetService;
import com.erdv.service.UtilisateurService;
import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

        @Autowired
        private AuthenticationManager authenticationManager;

        @Autowired
        private UtilisateurService utilisateurService;

        @Autowired
        private JwtService jwtService;

        @Autowired
        private PasswordResetService passwordResetService;

        @PostMapping("/register")
        public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
                Utilisateur utilisateur = new Utilisateur();
                utilisateur.setNom(request.getNom());
                utilisateur.setEmail(request.getEmail());
                utilisateur.setTelephone(request.getTelephone());
                utilisateur.setMotDePasse(request.getMotDePasse());
                utilisateur.setRole(Utilisateur.Role.USER);

                Utilisateur savedUtilisateur = utilisateurService.creerUtilisateur(utilisateur);

                String access = jwtService.generateAccessToken(savedUtilisateur);
                String refresh = jwtService.generateRefreshToken(savedUtilisateur);

                AuthResponse response = toAuthResponse(savedUtilisateur, access, refresh);

                return ResponseEntity.ok(response);
        }

        @PostMapping("/login")
        public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getMotDePasse()));

                Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
                String access = jwtService.generateAccessToken(utilisateur);
                String refresh = jwtService.generateRefreshToken(utilisateur);

                return ResponseEntity.ok(toAuthResponse(utilisateur, access, refresh));
        }

        @PostMapping("/refresh")
        public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
                try {
                        String refreshToken = request.getRefreshToken();
                        String email = jwtService.extractUsername(refreshToken);
                        UserDetails userDetails = utilisateurService.loadUserByUsername(email);
                        if (!jwtService.validateRefreshToken(refreshToken, userDetails)) {
                                throw new ApiException(HttpStatus.UNAUTHORIZED, "Jeton de rafraîchissement invalide");
                        }
                        Utilisateur u = (Utilisateur) userDetails;
                        String access = jwtService.generateAccessToken(userDetails);
                        String newRefresh = jwtService.generateRefreshToken(userDetails);
                        return ResponseEntity.ok(toAuthResponse(u, access, newRefresh));
                } catch (UsernameNotFoundException e) {
                        throw new ApiException(HttpStatus.UNAUTHORIZED, "Jeton de rafraîchissement invalide ou expiré");
                } catch (JwtException | IllegalArgumentException e) {
                        throw new ApiException(HttpStatus.UNAUTHORIZED, "Jeton de rafraîchissement invalide ou expiré");
                }
        }

        @PostMapping("/forgot-password")
        public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
                passwordResetService.requestPasswordReset(request.getEmail());
                return ResponseEntity.ok(Map.of(
                                "message",
                                "Si un compte correspond à cet email, un lien de réinitialisation a été envoyé."));
        }

        @PostMapping("/reset-password")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
                passwordResetService.resetPassword(request.getToken(), request.getNouveauMotDePasse());
        }

        private static AuthResponse toAuthResponse(Utilisateur u, String access, String refresh) {
                return new AuthResponse(
                                access,
                                refresh,
                                u.getId(),
                                u.getNom(),
                                u.getEmail(),
                                u.getRole().name(),
                                u.getPrestataireId());
        }
}