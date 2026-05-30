package com.erdv;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.erdv.dto.AuthRequest;
import com.erdv.dto.PrestationRequest;
import com.erdv.entity.Prestataire;
import com.erdv.entity.Utilisateur;
import com.erdv.repository.PrestataireRepository;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PrestataireCatalogueSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PrestataireRepository prestataireRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Prestataire prestataire;
    private Prestataire autrePrestataire;

    @BeforeEach
    void seed() {
        utilisateurRepository.deleteAll();
        prestataireRepository.deleteAll();

        prestataire = new Prestataire();
        prestataire.setNom("Dr. Martin");
        prestataire.setSpecialite("Généraliste");
        prestataire.setEmail("martin@erdv.com");
        prestataire = prestataireRepository.save(prestataire);

        autrePrestataire = new Prestataire();
        autrePrestataire.setNom("Dr. Autre");
        autrePrestataire.setSpecialite("Dentiste");
        autrePrestataire.setEmail("autre@erdv.com");
        autrePrestataire = prestataireRepository.save(autrePrestataire);

        Utilisateur prest = new Utilisateur();
        prest.setNom("Dr. Martin");
        prest.setEmail("martin@erdv.com");
        prest.setTelephone("0600000000");
        prest.setMotDePasse(passwordEncoder.encode("prestataire123"));
        prest.setRole(Utilisateur.Role.PRESTATAIRE);
        prest.setPrestataire(prestataire);
        utilisateurRepository.save(prest);

        Utilisateur user = new Utilisateur();
        user.setNom("Client");
        user.setEmail("user@erdv.com");
        user.setTelephone("0611111111");
        user.setMotDePasse(passwordEncoder.encode("user123"));
        user.setRole(Utilisateur.Role.USER);
        utilisateurRepository.save(user);
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
    void prestataireCanListOwnCatalogue() throws Exception {
        String token = loginToken("martin@erdv.com", "prestataire123");
        mockMvc.perform(get("/prestations/prestataire/" + prestataire.getId() + "/toutes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void prestataireForbiddenOnOtherCatalogue() throws Exception {
        String token = loginToken("martin@erdv.com", "prestataire123");
        mockMvc.perform(get("/prestations/prestataire/" + autrePrestataire.getId() + "/toutes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void userForbiddenOnCatalogueAdmin() throws Exception {
        String token = loginToken("user@erdv.com", "user123");
        mockMvc.perform(get("/prestations/prestataire/" + prestataire.getId() + "/toutes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void prestataireCanCreatePrestation() throws Exception {
        String token = loginToken("martin@erdv.com", "prestataire123");
        PrestationRequest req = new PrestationRequest();
        req.setPrestataireId(prestataire.getId());
        req.setNom("Consultation");
        req.setDureeMinutes(30);
        req.setActif(true);

        mockMvc.perform(post("/prestations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void prestataireCanAccessMonAgenda() throws Exception {
        String token = loginToken("martin@erdv.com", "prestataire123");
        mockMvc.perform(get("/rendez-vous/mon-agenda")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
