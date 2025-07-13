package com.erdv.service;

import com.erdv.entity.RendezVous;
import com.erdv.entity.Utilisateur;
import com.erdv.entity.Prestataire;
import com.erdv.entity.CreneauHoraire;
import com.erdv.repository.RendezVousRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RendezVousService {

    @Autowired
    private RendezVousRepository rendezVousRepository;

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private PrestataireService prestataireService;

    @Autowired
    private CreneauHoraireService creneauHoraireService;

    @Autowired
    private EmailService emailService;

    public List<RendezVous> getAllRendezVous() {
        return rendezVousRepository.findAll();
    }

    public List<RendezVous> getRendezVousByUtilisateur(Long utilisateurId) {
        return rendezVousRepository.findByUtilisateurIdOrderByDateHeureDesc(utilisateurId);
    }

    public List<RendezVous> getRendezVousByPrestataire(Long prestataireId) {
        return rendezVousRepository.findByPrestataireIdOrderByDateHeureDesc(prestataireId);
    }

    public List<RendezVous> getRendezVousByStatut(RendezVous.Statut statut) {
        return rendezVousRepository.findByStatutOrderByDateHeureDesc(statut);
    }

    public RendezVous getRendezVousById(Long id) {
        return rendezVousRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rendez-vous non trouvé"));
    }

    public RendezVous creerRendezVous(RendezVous rendezVous) {
        // Vérifier que le créneau est disponible
        CreneauHoraire creneau = creneauHoraireService.getCreneauById(rendezVous.getPrestataire().getId());
        if (!creneau.isDisponible()) {
            throw new RuntimeException("Ce créneau n'est plus disponible");
        }

        // Marquer le créneau comme indisponible
        creneauHoraireService.marquerCreneauIndisponible(creneau.getId());

        // Sauvegarder le rendez-vous
        RendezVous savedRendezVous = rendezVousRepository.save(rendezVous);

        // Envoyer l'email de confirmation
        try {
            emailService.envoyerConfirmationRendezVous(savedRendezVous);
        } catch (Exception e) {
            // Log l'erreur mais ne pas faire échouer la création du rendez-vous
            System.err.println("Erreur lors de l'envoi de l'email: " + e.getMessage());
        }

        return savedRendezVous;
    }

    public RendezVous updateRendezVous(Long id, RendezVous rendezVousDetails) {
        RendezVous rendezVous = getRendezVousById(id);

        rendezVous.setDateHeure(rendezVousDetails.getDateHeure());
        rendezVous.setService(rendezVousDetails.getService());
        rendezVous.setStatut(rendezVousDetails.getStatut());

        return rendezVousRepository.save(rendezVous);
    }

    public RendezVous confirmerRendezVous(Long id) {
        RendezVous rendezVous = getRendezVousById(id);
        rendezVous.setStatut(RendezVous.Statut.CONFIRME);

        RendezVous savedRendezVous = rendezVousRepository.save(rendezVous);

        // Envoyer l'email de confirmation
        try {
            emailService.envoyerConfirmationRendezVous(savedRendezVous);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de l'email: " + e.getMessage());
        }

        return savedRendezVous;
    }

    public RendezVous annulerRendezVous(Long id) {
        RendezVous rendezVous = getRendezVousById(id);
        rendezVous.setStatut(RendezVous.Statut.ANNULE);

        // Remettre le créneau comme disponible
        CreneauHoraire creneau = creneauHoraireService.getCreneauById(rendezVous.getPrestataire().getId());
        creneauHoraireService.marquerCreneauDisponible(creneau.getId());

        RendezVous savedRendezVous = rendezVousRepository.save(rendezVous);

        // Envoyer l'email d'annulation
        try {
            emailService.envoyerAnnulationRendezVous(savedRendezVous);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de l'email: " + e.getMessage());
        }

        return savedRendezVous;
    }

    public void deleteRendezVous(Long id) {
        RendezVous rendezVous = getRendezVousById(id);

        // Remettre le créneau comme disponible si le rendez-vous n'est pas annulé
        if (rendezVous.getStatut() != RendezVous.Statut.ANNULE) {
            CreneauHoraire creneau = creneauHoraireService.getCreneauById(rendezVous.getPrestataire().getId());
            creneauHoraireService.marquerCreneauDisponible(creneau.getId());
        }

        rendezVousRepository.deleteById(id);
    }
}