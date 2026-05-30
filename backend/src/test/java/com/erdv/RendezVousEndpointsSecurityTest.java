package com.erdv;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.erdv.dto.AuthRequest;
import com.erdv.entity.Utilisateur;
import com.erdv.repository.RefreshTokenRepository;
import com.erdv.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Vérifie que les règles AntPath sur /rendez-vous/** autorisent bien mes-rendez-vous (USER)
 * et /tous (ADMIN seulement).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RendezVousEndpointsSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void seedUsers() {
        refreshTokenRepository.deleteAll();
        utilisateurRepository.deleteAll();
        Utilisateur user = new Utilisateur();
        user.setNom("User");
        user.setEmail("user@erdv.com");
        user.setTelephone("0600000000");
        user.setMotDePasse(passwordEncoder.encode("user123"));
        user.setRole(Utilisateur.Role.USER);
        utilisateurRepository.save(user);

        Utilisateur admin = new Utilisateur();
        admin.setNom("Admin");
        admin.setEmail("admin@erdv.com");
        admin.setTelephone("0600000001");
        admin.setMotDePasse(passwordEncoder.encode("admin123"));
        admin.setRole(Utilisateur.Role.ADMIN);
        utilisateurRepository.save(admin);
    }

    private String loginToken(String email, String password) throws Exception {
        AuthRequest body = new AuthRequest();
        body.setEmail(email);
        body.setMotDePasse(password);
        String json = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(json).get("token").asText();
    }

    @Test
    void userCanGetMesRendezVous() throws Exception {
        String token = loginToken("user@erdv.com", "user123");
        mockMvc.perform(get("/rendez-vous/mes-rendez-vous")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void userForbiddenOnTous() throws Exception {
        String token = loginToken("user@erdv.com", "user123");
        mockMvc.perform(get("/rendez-vous/tous")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanGetTous() throws Exception {
        String token = loginToken("admin@erdv.com", "admin123");
        mockMvc.perform(get("/rendez-vous/tous")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void adminCanGetMesRendezVous() throws Exception {
        String token = loginToken("admin@erdv.com", "admin123");
        mockMvc.perform(get("/rendez-vous/mes-rendez-vous")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
