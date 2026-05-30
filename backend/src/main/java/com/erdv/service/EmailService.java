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

    public void envoyerAccuseReceptionRendezVous(RendezVous rendezVous) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(rendezVous.getUtilisateur().getEmail());
        message.setSubject("Demande de rendez-vous enregistrée — eRDV");

        String contenu = String.format(
                "Bonjour %s,\n\n"
                        + "Votre demande de rendez-vous a bien été enregistrée et est en attente de validation.\n\n"
                        + "Détails :\n"
                        + "- Prestataire : %s (%s)\n"
                        + "- Date et heure : %s\n"
                        + "- Motif / service : %s\n"
                        + "- Statut : en attente de confirmation\n\n"
                        + "Vous recevrez un email dès que le rendez-vous sera confirmé.\n\n"
                        + "Cordialement,\n"
                        + "L'équipe eRDV",
                rendezVous.getUtilisateur().getNom(),
                rendezVous.getPrestataire().getNom(),
                rendezVous.getPrestataire().getSpecialite(),
                rendezVous.getDateHeure().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")),
                rendezVous.getService());

        message.setText(contenu);
        mailSender.send(message);
    }

    public void envoyerConfirmationRendezVous(RendezVous rendezVous) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(rendezVous.getUtilisateur().getEmail());
        message.setSubject("Confirmation de votre rendez-vous — eRDV");

        String contenu = String.format(
                "Bonjour %s,\n\n"
                        + "Votre rendez-vous est confirmé.\n\n"
                        + "Détails :\n"
                        + "- Prestataire : %s (%s)\n"
                        + "- Date et heure : %s\n"
                        + "- Motif / service : %s\n"
                        + "- Statut : confirmé\n\n"
                        + "Merci de votre confiance.\n\n"
                        + "Cordialement,\n"
                        + "L'équipe eRDV",
                rendezVous.getUtilisateur().getNom(),
                rendezVous.getPrestataire().getNom(),
                rendezVous.getPrestataire().getSpecialite(),
                rendezVous.getDateHeure().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")),
                rendezVous.getService());

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

    public void envoyerReprogrammationRendezVous(RendezVous rendezVous, java.time.LocalDateTime ancienneDate) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(rendezVous.getUtilisateur().getEmail());
        message.setSubject("Reprogrammation de votre rendez-vous — eRDV");

        String contenu = String.format(
                "Bonjour %s,\n\n"
                        + "Votre rendez-vous a été reprogrammé.\n\n"
                        + "Ancienne date : %s\n"
                        + "Nouvelle date : %s\n\n"
                        + "Détails :\n"
                        + "- Prestataire : %s (%s)\n"
                        + "- Motif / service : %s\n"
                        + "- Statut : %s\n\n"
                        + "Cordialement,\n"
                        + "L'équipe eRDV",
                rendezVous.getUtilisateur().getNom(),
                ancienneDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")),
                rendezVous.getDateHeure().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")),
                rendezVous.getPrestataire().getNom(),
                rendezVous.getPrestataire().getSpecialite(),
                rendezVous.getService(),
                rendezVous.getStatut() == RendezVous.Statut.CONFIRME ? "confirmé" : "en attente de confirmation");

        message.setText(contenu);
        mailSender.send(message);
    }

    public void envoyerLienReinitialisationMotDePasse(String email, String resetUrl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Réinitialisation de votre mot de passe eRDV");
        message.setText(
                "Bonjour,\n\n"
                        + "Pour définir un nouveau mot de passe, ouvrez ce lien dans votre navigateur :\n\n"
                        + resetUrl
                        + "\n\n"
                        + "Ce lien expire dans une heure. Si vous n'avez pas demandé cette réinitialisation, ignorez ce message.\n\n"
                        + "L'équipe eRDV");
        mailSender.send(message);
    }
}