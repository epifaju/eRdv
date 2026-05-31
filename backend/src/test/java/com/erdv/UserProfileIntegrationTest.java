package com.erdv;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.erdv.dto.AuthRequest;
import com.erdv.dto.UpdateProfileRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserProfileIntegrationTest {

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

    private String loginToken() throws Exception {
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
        return objectMapper.readTree(json).get("token").asText();
    }

    @Test
    void profileReturnsSmsConsentFalseByDefault() throws Exception {
        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + loginToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consentementSmsRappels").value(false))
                .andExpect(jsonPath("$.consentementSmsRappelsAt").doesNotExist());
    }

    @Test
    void canOptInToSmsRemindersWithValidPhone() throws Exception {
        UpdateProfileRequest body = new UpdateProfileRequest();
        body.setNom("Test User");
        body.setEmail("test@erdv.com");
        body.setTelephone("06 12 34 56 78");
        body.setConsentementSmsRappels(true);

        mockMvc.perform(put("/users/me")
                        .header("Authorization", "Bearer " + loginToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consentementSmsRappels").value(true))
                .andExpect(jsonPath("$.consentementSmsRappelsAt", notNullValue()));
    }

    @Test
    void cannotOptInToSmsWithoutValidPhone() throws Exception {
        UpdateProfileRequest body = new UpdateProfileRequest();
        body.setNom("Test User");
        body.setEmail("test@erdv.com");
        body.setTelephone("invalid");
        body.setConsentementSmsRappels(true);

        mockMvc.perform(put("/users/me")
                        .header("Authorization", "Bearer " + loginToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }
}
