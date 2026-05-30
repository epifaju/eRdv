package com.erdv.service;

import com.erdv.entity.CreneauHoraire;
import com.erdv.entity.RendezVous;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm");

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmailTemplateRenderer templateRenderer;

    @Value("${app.frontend.base-url:http://localhost:3001}")
    private String frontendBaseUrl;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${app.mail.from:noreply@erdv.local}")
    private String mailFrom;

    public void envoyerAccuseReceptionRendezVous(RendezVous rendezVous) {
        String html = templateRenderer.render(
                "Demande enregistrée",
                "Bonjour " + rendezVous.getUtilisateur().getNom() + ",",
                "<p style=\"margin:0 0 8px;\">Votre demande de rendez-vous a bien été enregistrée et est <strong>en attente de validation</strong>.</p>"
                        + "<p style=\"margin:0;\">Vous recevrez un e-mail dès que le prestataire aura confirmé votre rendez-vous.</p>",
                buildRendezVousDetails(rendezVous, "En attente de confirmation"),
                "Voir mes rendez-vous",
                mesRendezVousUrl(),
                null);
        sendHtml(rendezVous.getUtilisateur().getEmail(), "Demande de rendez-vous enregistrée — eRDV", html);
    }

    public void envoyerConfirmationRendezVous(RendezVous rendezVous) {
        String html = templateRenderer.render(
                "Rendez-vous confirmé",
                "Bonjour " + rendezVous.getUtilisateur().getNom() + ",",
                "<p style=\"margin:0;\">Bonne nouvelle : votre rendez-vous est <strong>confirmé</strong>. Merci de votre confiance.</p>",
                buildRendezVousDetails(rendezVous, "Confirmé"),
                "Voir mon rendez-vous",
                mesRendezVousUrl(),
                "En cas d'empêchement, pensez à annuler ou reprogrammer depuis votre espace personnel.");
        sendHtml(rendezVous.getUtilisateur().getEmail(), "Confirmation de votre rendez-vous — eRDV", html);
    }

    public void envoyerRefusRendezVous(RendezVous rendezVous) {
        String html = templateRenderer.render(
                "Demande non retenue",
                "Bonjour " + rendezVous.getUtilisateur().getNom() + ",",
                "<p style=\"margin:0;\">Votre demande de rendez-vous n'a pas pu être acceptée par le prestataire. "
                        + "Vous pouvez choisir un autre créneau ou un autre prestataire.</p>",
                buildRendezVousDetails(rendezVous, "Refusé"),
                "Prendre un nouveau rendez-vous",
                prestatairesUrl(),
                null);
        sendHtml(rendezVous.getUtilisateur().getEmail(), "Demande de rendez-vous refusée — eRDV", html);
    }

    public void envoyerAnnulationRendezVous(RendezVous rendezVous) {
        String html = templateRenderer.render(
                "Rendez-vous annulé",
                "Bonjour " + rendezVous.getUtilisateur().getNom() + ",",
                "<p style=\"margin:0;\">Votre rendez-vous a été annulé comme demandé.</p>",
                buildRendezVousDetails(rendezVous, "Annulé"),
                "Prendre un nouveau rendez-vous",
                prestatairesUrl(),
                null);
        sendHtml(rendezVous.getUtilisateur().getEmail(), "Annulation de votre rendez-vous — eRDV", html);
    }

    public void envoyerAnnulationParPrestataire(RendezVous rendezVous) {
        String html = templateRenderer.render(
                "Rendez-vous annulé par le prestataire",
                "Bonjour " + rendezVous.getUtilisateur().getNom() + ",",
                "<p style=\"margin:0;\">Votre rendez-vous confirmé a été annulé par le prestataire. "
                        + "Nous vous invitons à réserver un nouveau créneau.</p>",
                buildRendezVousDetails(rendezVous, "Annulé"),
                "Prendre un nouveau rendez-vous",
                prestatairesUrl(),
                null);
        sendHtml(rendezVous.getUtilisateur().getEmail(), "Annulation de rendez-vous — eRDV", html);
    }

    public void envoyerNouvelleDemandeAuPrestataire(RendezVous rendezVous) {
        String clientNom = rendezVous.getUtilisateur().getNom();
        String clientEmail = rendezVous.getUtilisateur().getEmail();
        String clientTel = rendezVous.getUtilisateur().getTelephone();

        String intro = "<p style=\"margin:0 0 8px;\">Une nouvelle demande de rendez-vous vient d'être enregistrée.</p>"
                + "<p style=\"margin:0;\">Connectez-vous à votre espace prestataire pour <strong>confirmer</strong> ou <strong>refuser</strong> cette demande.</p>";

        String details = templateRenderer.detailsBlock(
                "Client", EmailTemplateRenderer.escapeHtml(clientNom),
                "E-mail client", EmailTemplateRenderer.escapeHtml(clientEmail),
                "Téléphone", clientTel != null && !clientTel.isBlank()
                        ? EmailTemplateRenderer.escapeHtml(clientTel)
                        : "—",
                "Prestation / service", EmailTemplateRenderer.escapeHtml(rendezVous.getService()),
                "Date et heure", formatPlage(rendezVous),
                "Statut", "En attente");

        String html = templateRenderer.render(
                "Nouvelle demande de rendez-vous",
                "Bonjour " + rendezVous.getPrestataire().getNom() + ",",
                intro,
                details,
                "Gérer mes rendez-vous",
                prestataireAgendaUrl(),
                "Cet e-mail est envoyé automatiquement à chaque nouvelle réservation.");

        sendHtml(rendezVous.getPrestataire().getEmail(), "Nouvelle demande de rendez-vous — eRDV", html);
    }

    public void envoyerRappelJ1(RendezVous rendezVous) {
        String statutLabel = rendezVous.getStatut() == RendezVous.Statut.CONFIRME
                ? "Confirmé"
                : "En attente de confirmation";
        String intro = rendezVous.getStatut() == RendezVous.Statut.CONFIRME
                ? "<p style=\"margin:0;\">Rappel : vous avez un rendez-vous <strong>demain</strong>.</p>"
                : "<p style=\"margin:0;\">Rappel : vous avez une demande de rendez-vous prévue <strong>demain</strong>, "
                        + "toujours en attente de confirmation par le prestataire.</p>";

        String html = templateRenderer.render(
                "Rappel J-1",
                "Bonjour " + rendezVous.getUtilisateur().getNom() + ",",
                intro,
                buildRendezVousDetails(rendezVous, statutLabel),
                "Voir mon rendez-vous",
                mesRendezVousUrl(),
                null);
        sendHtml(rendezVous.getUtilisateur().getEmail(), "Rappel : rendez-vous demain — eRDV", html);
    }

    public void envoyerRappelH2(RendezVous rendezVous) {
        String html = templateRenderer.render(
                "Rappel H-2",
                "Bonjour " + rendezVous.getUtilisateur().getNom() + ",",
                "<p style=\"margin:0;\">Rappel : votre rendez-vous a lieu dans environ <strong>2 heures</strong>.</p>",
                buildRendezVousDetails(rendezVous, "Confirmé"),
                "Voir mon rendez-vous",
                mesRendezVousUrl(),
                "Pensez à vous présenter à l'heure. En cas d'empêchement, contactez le prestataire.");
        sendHtml(rendezVous.getUtilisateur().getEmail(), "Rappel : rendez-vous dans 2 heures — eRDV", html);
    }

    public void envoyerReprogrammationRendezVous(RendezVous rendezVous, LocalDateTime ancienneDate) {
        String intro = "<p style=\"margin:0 0 12px;\">Votre rendez-vous a été reprogrammé.</p>"
                + "<p style=\"margin:0 0 4px;\"><strong>Ancienne date :</strong> "
                + EmailTemplateRenderer.escapeHtml(ancienneDate.format(DATE_FORMAT))
                + "</p>"
                + "<p style=\"margin:0;\"><strong>Nouvelle date :</strong> "
                + EmailTemplateRenderer.escapeHtml(formatPlage(rendezVous))
                + "</p>";

        String statut = rendezVous.getStatut() == RendezVous.Statut.CONFIRME
                ? "Confirmé"
                : "En attente de confirmation";

        String html = templateRenderer.render(
                "Rendez-vous reprogrammé",
                "Bonjour " + rendezVous.getUtilisateur().getNom() + ",",
                intro,
                buildRendezVousDetails(rendezVous, statut),
                "Voir mon rendez-vous",
                mesRendezVousUrl(),
                null);
        sendHtml(rendezVous.getUtilisateur().getEmail(), "Reprogrammation de votre rendez-vous — eRDV", html);
    }

    public void envoyerLienReinitialisationMotDePasse(String email, String resetUrl) {
        String html = templateRenderer.render(
                "Réinitialisation du mot de passe",
                "Bonjour,",
                "<p style=\"margin:0 0 8px;\">Vous avez demandé à réinitialiser votre mot de passe eRDV.</p>"
                        + "<p style=\"margin:0;\">Ce lien expire dans une heure. Si vous n'êtes pas à l'origine de cette demande, ignorez cet e-mail.</p>",
                null,
                "Définir un nouveau mot de passe",
                resetUrl,
                null);
        sendHtml(email, "Réinitialisation de votre mot de passe eRDV", html);
    }

    private String buildRendezVousDetails(RendezVous rendezVous, String statutLabel) {
        return templateRenderer.detailsBlock(
                "Prestataire",
                EmailTemplateRenderer.escapeHtml(rendezVous.getPrestataire().getNom()),
                "Spécialité",
                EmailTemplateRenderer.escapeHtml(rendezVous.getPrestataire().getSpecialite()),
                "Prestation / service",
                EmailTemplateRenderer.escapeHtml(rendezVous.getService()),
                "Date et heure",
                EmailTemplateRenderer.escapeHtml(formatPlage(rendezVous)),
                "Statut",
                EmailTemplateRenderer.escapeHtml(statutLabel));
    }

    private String formatPlage(RendezVous rendezVous) {
        LocalDateTime debut = rendezVous.getDateHeure();
        int duree = computeDureeMinutes(rendezVous);
        if (duree <= rendezVous.getCreneau().getDureeMinutes()) {
            return debut.format(DATE_FORMAT);
        }
        LocalDateTime fin = debut.plusMinutes(duree);
        DateTimeFormatter heure = DateTimeFormatter.ofPattern("HH:mm");
        return debut.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                + " de "
                + debut.format(heure)
                + " à "
                + fin.format(heure);
    }

    private int computeDureeMinutes(RendezVous rendezVous) {
        if (rendezVous.getPrestation() != null) {
            return rendezVous.getPrestation().getDureeMinutes();
        }
        List<CreneauHoraire> slots = rendezVous.getCreneauxReserves();
        if (slots != null && !slots.isEmpty()) {
            return slots.stream().mapToInt(CreneauHoraire::getDureeMinutes).sum();
        }
        return rendezVous.getCreneau().getDureeMinutes();
    }

    private void sendHtml(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(resolveFrom());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(buildPlainTextFallback(subject, htmlBody), htmlBody);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de la préparation de l'e-mail", e);
        }
    }

    private String resolveFrom() {
        if (mailUsername != null && !mailUsername.isBlank()) {
            return mailUsername;
        }
        return mailFrom;
    }

    private String buildPlainTextFallback(String subject, String html) {
        return subject + "\n\n(Consultez la version HTML de cet e-mail pour le détail et les liens.)\n\n"
                + html.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
    }

    private String mesRendezVousUrl() {
        return trimTrailingSlash(frontendBaseUrl) + "/mes-rendez-vous";
    }

    private String prestatairesUrl() {
        return trimTrailingSlash(frontendBaseUrl) + "/prestataires";
    }

    private String prestataireAgendaUrl() {
        return trimTrailingSlash(frontendBaseUrl) + "/prestataire";
    }

    private static String trimTrailingSlash(String url) {
        if (url == null) {
            return "";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
