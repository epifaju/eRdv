package com.erdv.service;

import com.erdv.entity.RendezVous;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void envoyerConfirmationRendezVous(RendezVous rendezVous) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(rendezVous.getUtilisateur().getEmail());
        message.setSubject("Confirmation de votre rendez-vous");

        String contenu = String.format(
                "Bonjour %s,\n\n" +
                        "Votre rendez-vous a été confirmé avec succès.\n\n" +
                        "Détails du rendez-vous :\n" +
                        "- Prestataire : %s (%s)\n" +
                        "- Date et heure : %s\n" +
                        "- Service : %s\n" +
                        "- Statut : %s\n\n" +
                        "Merci de votre confiance.\n\n" +
                        "Cordialement,\n" +
                        "L'équipe eRDV",
                rendezVous.getUtilisateur().getNom(),
                rendezVous.getPrestataire().getNom(),
                rendezVous.getPrestataire().getSpecialite(),
                rendezVous.getDateHeure().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")),
                rendezVous.getService(),
                rendezVous.getStatut().toString());

        message.setText(contenu);
        mailSender.send(message);
    }

    public void envoyerAnnulationRendezVous(RendezVous rendezVous) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(rendezVous.getUtilisateur().getEmail());
        message.setSubject("Annulation de votre rendez-vous");

        String contenu = String.format(
                "Bonjour %s,\n\n" +
                        "Votre rendez-vous a été annulé.\n\n" +
                        "Détails du rendez-vous annulé :\n" +
                        "- Prestataire : %s (%s)\n" +
                        "- Date et heure : %s\n" +
                        "- Service : %s\n\n" +
                        "Pour prendre un nouveau rendez-vous, connectez-vous à votre espace personnel.\n\n" +
                        "Cordialement,\n" +
                        "L'équipe eRDV",
                rendezVous.getUtilisateur().getNom(),
                rendezVous.getPrestataire().getNom(),
                rendezVous.getPrestataire().getSpecialite(),
                rendezVous.getDateHeure().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")),
                rendezVous.getService());

        message.setText(contenu);
        mailSender.send(message);
    }
}