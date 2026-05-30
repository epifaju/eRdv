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

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

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
    void seedUser() {
        refreshTokenRepository.deleteAll();
        utilisateurRepository.deleteAll();
        Utilisateur u = new Utilisateur();
        u.setNom("Test User");
        u.setEmail("test@erdv.com");
        u.setTelephone("0600000000");
        u.setMotDePasse(passwordEncoder.encode("secret12"));
        u.setRole(Utilisateur.Role.USER);
        utilisateurRepository.save(u);
    }

    @Test
    void loginReturnsAccessAndRefreshTokens() throws Exception {
        AuthRequest body = new AuthRequest();
        body.setEmail("test@erdv.com");
        body.setMotDePasse("secret12");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.email").value("test@erdv.com"));
    }

    @Test
    void refreshExchangesRefreshToken() throws Exception {
        AuthRequest body = new AuthRequest();
        body.setEmail("test@erdv.com");
        body.setMotDePasse("secret12");

        String json = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String refresh = objectMapper.readTree(json).get("refreshToken").asText();

        String refreshBody = "{\"refreshToken\":\"" + refresh + "\"}";
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()));
    }

    @Test
    void logoutRevokesRefreshToken() throws Exception {
        AuthRequest body = new AuthRequest();
        body.setEmail("test@erdv.com");
        body.setMotDePasse("secret12");

        String json = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String refresh = objectMapper.readTree(json).get("refreshToken").asText();

        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refresh + "\"}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refresh + "\"}"))
                .andExpect(status().isUnauthorized());
    }
}
