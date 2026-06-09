package com.erdv.service;

import com.erdv.entity.CreneauHoraire;
import com.erdv.entity.Prestataire;
import com.erdv.entity.RendezVous;
import com.erdv.entity.Utilisateur;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @InjectMocks
    private EmailService emailService;

    @Mock
    private JavaMailSender mailSender;

    private final EmailTemplateRenderer templateRenderer = new EmailTemplateRenderer();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "templateRenderer", templateRenderer);
        ReflectionTestUtils.setField(emailService, "frontendBaseUrl", "http://localhost:3001");
        ReflectionTestUtils.setField(emailService, "mailFrom", "noreply@erdv.local");
        ReflectionTestUtils.setField(emailService, "mailUsername", "");
    }

    @Test
    void envoyerConfirmationRendezVousEnvoieEmail() throws Exception {
        RendezVous rdv = sampleRendezVous();
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.envoyerConfirmationRendezVous(rdv);

        verify(mailSender).send(mimeMessage);
    }

    @Test
    void envoyerLienReinitialisationMotDePasse() throws Exception {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.envoyerLienReinitialisationMotDePasse("user@test.com", "http://localhost/reset?token=abc");

        verify(mailSender).send(mimeMessage);
    }

    private static RendezVous sampleRendezVous() {
        Utilisateur u = new Utilisateur();
        u.setNom("Client");
        u.setEmail("client@test.com");

        Prestataire p = new Prestataire();
        p.setNom("Dr Martin");
        p.setSpecialite("Généraliste");

        CreneauHoraire c = new CreneauHoraire();
        c.setDureeMinutes(30);
        c.setDateHeure(LocalDateTime.now().plusDays(1));

        RendezVous rdv = new RendezVous();
        rdv.setUtilisateur(u);
        rdv.setPrestataire(p);
        rdv.setCreneau(c);
        rdv.setService("Consultation");
        rdv.setDateHeure(c.getDateHeure());
        rdv.setStatut(RendezVous.Statut.CONFIRME);
        return rdv;
    }
}
