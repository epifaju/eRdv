package com.erdv;

import com.erdv.dto.CreateRendezVousRequest;
import com.erdv.entity.CreneauHoraire;
import com.erdv.entity.Prestataire;
import com.erdv.entity.Prestation;
import com.erdv.entity.Utilisateur;
import com.erdv.exception.ApiException;
import com.erdv.repository.CreneauHoraireRepository;
import com.erdv.repository.PrestationRepository;
import com.erdv.repository.PrestataireRepository;
import com.erdv.repository.RendezVousRepository;
import com.erdv.repository.UtilisateurRepository;
import com.erdv.service.RendezVousService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RendezVousServiceTest {

    @Autowired
    private RendezVousService rendezVousService;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PrestataireRepository prestataireRepository;

    @Autowired
    private CreneauHoraireRepository creneauHoraireRepository;

    @Autowired
    private RendezVousRepository rendezVousRepository;

    @Autowired
    private PrestationRepository prestationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Utilisateur client;
    private CreneauHoraire creneau;

    @BeforeEach
    void setUp() {
        rendezVousRepository.deleteAll();
        prestationRepository.deleteAll();
        creneauHoraireRepository.deleteAll();
        prestataireRepository.deleteAll();
        utilisateurRepository.deleteAll();

        client = new Utilisateur();
        client.setNom("Client Test");
        client.setEmail("client@test.com");
        client.setTelephone("0600000000");
        client.setMotDePasse(passwordEncoder.encode("pass123"));
        client.setRole(Utilisateur.Role.USER);
        client = utilisateurRepository.save(client);

        Prestataire p = new Prestataire();
        p.setNom("Dr. Test");
        p.setSpecialite("Généraliste");
        p.setEmail("dr@test.com");
        p = prestataireRepository.save(p);

        creneau = new CreneauHoraire();
        creneau.setPrestataire(p);
        creneau.setDateHeure(java.time.LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0));
        creneau.setDureeMinutes(30);
        creneau.setDisponible(true);
        creneau = creneauHoraireRepository.save(creneau);
    }

    @Test
    void reservationReussieEnAttente() {
        CreateRendezVousRequest req = new CreateRendezVousRequest();
        req.setCreneauId(creneau.getId());
        req.setService("Consultation");

        var response = rendezVousService.creerRendezVous(client, req);

        assertEquals("EN_ATTENTE", response.getStatut());
        assertEquals(false, creneauHoraireRepository.findById(creneau.getId()).orElseThrow().isDisponible());
    }

    @Test
    void doubleReservationMemeCreneauRefusee() {
        CreateRendezVousRequest req = new CreateRendezVousRequest();
        req.setCreneauId(creneau.getId());
        req.setService("Consultation");
        rendezVousService.creerRendezVous(client, req);

        Utilisateur autre = utilisateurRepository.save(buildUser("autre@test.com", "Autre"));

        CreateRendezVousRequest req2 = new CreateRendezVousRequest();
        req2.setCreneauId(creneau.getId());
        req2.setService("Consultation");

        ApiException ex = assertThrows(ApiException.class,
                () -> rendezVousService.creerRendezVous(autre, req2));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test
    void reprogrammationLibereAncienCreneau() {
        CreateRendezVousRequest req = new CreateRendezVousRequest();
        req.setCreneauId(creneau.getId());
        req.setService("Consultation");
        var rdv = rendezVousService.creerRendezVous(client, req);

        CreneauHoraire autreCreneau = new CreneauHoraire();
        autreCreneau.setPrestataire(creneau.getPrestataire());
        autreCreneau.setDateHeure(creneau.getDateHeure().plusDays(1));
        autreCreneau.setDisponible(true);
        autreCreneau = creneauHoraireRepository.save(autreCreneau);

        var updated = rendezVousService.reprogrammerRendezVous(rdv.getId(), autreCreneau.getId(), client);

        assertEquals(autreCreneau.getId(), updated.getCreneau().getId());
        assertEquals(true, creneauHoraireRepository.findById(creneau.getId()).orElseThrow().isDisponible());
        assertEquals(false, creneauHoraireRepository.findById(autreCreneau.getId()).orElseThrow().isDisponible());
    }

    @Test
    void reprogrammationRdvAnnuleRefusee() {
        CreateRendezVousRequest req = new CreateRendezVousRequest();
        req.setCreneauId(creneau.getId());
        req.setService("Consultation");
        var rdv = rendezVousService.creerRendezVous(client, req);
        rendezVousService.annulerRendezVous(rdv.getId(), client);

        CreneauHoraire autreCreneau = new CreneauHoraire();
        autreCreneau.setPrestataire(creneau.getPrestataire());
        autreCreneau.setDateHeure(creneau.getDateHeure().plusDays(2));
        autreCreneau.setDisponible(true);
        autreCreneau = creneauHoraireRepository.save(autreCreneau);
        final Long autreCreneauId = autreCreneau.getId();

        ApiException ex = assertThrows(ApiException.class,
                () -> rendezVousService.reprogrammerRendezVous(rdv.getId(), autreCreneauId, client));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void reservationPrestationLongueOccupePlusieursCreneaux() {
        Prestataire p = creneau.getPrestataire();
        Prestation prestation = new Prestation();
        prestation.setPrestataire(p);
        prestation.setNom("Consultation longue");
        prestation.setDureeMinutes(60);
        prestation.setActif(true);
        prestation = prestationRepository.save(prestation);

        CreneauHoraire slot2 = new CreneauHoraire();
        slot2.setPrestataire(p);
        slot2.setDateHeure(creneau.getDateHeure().plusMinutes(30));
        slot2.setDureeMinutes(30);
        slot2.setDisponible(true);
        slot2 = creneauHoraireRepository.save(slot2);

        CreateRendezVousRequest req = new CreateRendezVousRequest();
        req.setCreneauId(creneau.getId());
        req.setPrestationId(prestation.getId());

        var response = rendezVousService.creerRendezVous(client, req);

        assertEquals("EN_ATTENTE", response.getStatut());
        assertEquals(2, response.getNbCreneaux().intValue());
        assertEquals(false, creneauHoraireRepository.findById(creneau.getId()).orElseThrow().isDisponible());
        assertEquals(false, creneauHoraireRepository.findById(slot2.getId()).orElseThrow().isDisponible());
    }

    @Test
    void annulationClientAutoriseePlusDe24h() {
        CreateRendezVousRequest req = new CreateRendezVousRequest();
        req.setCreneauId(creneau.getId());
        req.setService("Consultation");
        var rdv = rendezVousService.creerRendezVous(client, req);

        var cancelled = rendezVousService.annulerRendezVous(rdv.getId(), client);

        assertEquals("ANNULE", cancelled.getStatut());
        assertEquals(true, creneauHoraireRepository.findById(creneau.getId()).orElseThrow().isDisponible());
    }

    @Test
    void annulationClientRefuseeMoinsDe24h() {
        creneau.setDateHeure(java.time.LocalDateTime.now().plusHours(12).withSecond(0).withNano(0));
        creneau = creneauHoraireRepository.save(creneau);

        CreateRendezVousRequest req = new CreateRendezVousRequest();
        req.setCreneauId(creneau.getId());
        req.setService("Consultation");
        var rdv = rendezVousService.creerRendezVous(client, req);

        ApiException ex = assertThrows(ApiException.class,
                () -> rendezVousService.annulerRendezVous(rdv.getId(), client));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    private Utilisateur buildUser(String email, String nom) {
        Utilisateur u = new Utilisateur();
        u.setNom(nom);
        u.setEmail(email);
        u.setTelephone("0611111111");
        u.setMotDePasse(passwordEncoder.encode("pass123"));
        u.setRole(Utilisateur.Role.USER);
        return u;
    }
}
