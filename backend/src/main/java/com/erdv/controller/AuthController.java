package com.erdv.controller;

import com.erdv.dto.AuthRequest;
import com.erdv.dto.AuthResponse;
import com.erdv.dto.RegisterRequest;
import com.erdv.entity.Utilisateur;
import com.erdv.service.JwtService;
import com.erdv.service.UtilisateurService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

        @Autowired
        private AuthenticationManager authenticationManager;

        @Autowired
        private UtilisateurService utilisateurService;

        @Autowired
        private JwtService jwtService;

        @PostMapping("/register")
        public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
                Utilisateur utilisateur = new Utilisateur();
                utilisateur.setNom(request.getNom());
                utilisateur.setEmail(request.getEmail());
                utilisateur.setTelephone(request.getTelephone());
                utilisateur.setMotDePasse(request.getMotDePasse());
                utilisateur.setRole(Utilisateur.Role.USER);

                Utilisateur savedUtilisateur = utilisateurService.creerUtilisateur(utilisateur);

                String token = jwtService.generateToken(savedUtilisateur);

                AuthResponse response = new AuthResponse(
                                token,
                                savedUtilisateur.getId(),
                                savedUtilisateur.getNom(),
                                savedUtilisateur.getEmail(),
                                savedUtilisateur.getRole().name());

                return ResponseEntity.ok(response);
        }

        @PostMapping("/login")
        public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getMotDePasse()));

                Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
                String token = jwtService.generateToken(utilisateur);

                AuthResponse response = new AuthResponse(
                                token,
                                utilisateur.getId(),
                                utilisateur.getNom(),
                                utilisateur.getEmail(),
                                utilisateur.getRole().name());

                return ResponseEntity.ok(response);
        }
}