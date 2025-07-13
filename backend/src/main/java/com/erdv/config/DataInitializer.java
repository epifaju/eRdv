package com.erdv.config;

import com.erdv.entity.Utilisateur;
import com.erdv.entity.Prestataire;
import com.erdv.entity.CreneauHoraire;
import com.erdv.entity.RendezVous;
import com.erdv.repository.UtilisateurRepository;
import com.erdv.repository.PrestataireRepository;
import com.erdv.repository.CreneauHoraireRepository;
import com.erdv.repository.RendezVousRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PrestataireRepository prestataireRepository;

    @Autowired
    private CreneauHoraireRepository creneauHoraireRepository;

    @Autowired
    private RendezVousRepository rendezVousRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Créer un utilisateur admin
        if (utilisateurRepository.findByEmail("admin@erdv.com").isEmpty()) {
            Utilisateur admin = new Utilisateur();
            admin.setNom("Administrateur");
            admin.setEmail("admin@erdv.com");
            admin.setTelephone("0123456789");
            admin.setMotDePasse(passwordEncoder.encode("admin123"));
            admin.setRole(Utilisateur.Role.ADMIN);
            utilisateurRepository.save(admin);
        }

        // Créer un utilisateur normal
        if (utilisateurRepository.findByEmail("user@erdv.com").isEmpty()) {
            Utilisateur user = new Utilisateur();
            user.setNom("Utilisateur Test");
            user.setEmail("user@erdv.com");
            user.setTelephone("0987654321");
            user.setMotDePasse(passwordEncoder.encode("user123"));
            user.setRole(Utilisateur.Role.USER);
            utilisateurRepository.save(user);
        }

        // Créer des prestataires
        if (prestataireRepository.count() == 0) {
            Prestataire prestataire1 = new Prestataire();
            prestataire1.setNom("Dr. Martin");
            prestataire1.setSpecialite("Médecin généraliste");
            prestataire1.setEmail("martin@erdv.com");
            prestataireRepository.save(prestataire1);

            Prestataire prestataire2 = new Prestataire();
            prestataire2.setNom("Dr. Dubois");
            prestataire2.setSpecialite("Dentiste");
            prestataire2.setEmail("dubois@erdv.com");
            prestataireRepository.save(prestataire2);

            Prestataire prestataire3 = new Prestataire();
            prestataire3.setNom("Mme. Laurent");
            prestataire3.setSpecialite("Kinésithérapeute");
            prestataire3.setEmail("laurent@erdv.com");
            prestataireRepository.save(prestataire3);
        }

        // Créer des créneaux horaires
        if (creneauHoraireRepository.count() == 0) {
            Prestataire prestataire1 = prestataireRepository.findAll().get(0);

            // Créneaux pour aujourd'hui
            LocalDateTime now = LocalDateTime.now();
            for (int i = 9; i <= 17; i++) {
                CreneauHoraire creneau = new CreneauHoraire();
                creneau.setPrestataire(prestataire1);
                creneau.setDateHeure(now.toLocalDate().atTime(i, 0));
                creneau.setDisponible(true);
                creneauHoraireRepository.save(creneau);
            }

            // Créneaux pour demain
            LocalDateTime tomorrow = now.plusDays(1);
            for (int i = 9; i <= 17; i++) {
                CreneauHoraire creneau = new CreneauHoraire();
                creneau.setPrestataire(prestataire1);
                creneau.setDateHeure(tomorrow.toLocalDate().atTime(i, 0));
                creneau.setDisponible(true);
                creneauHoraireRepository.save(creneau);
            }
        }

        // Créer un rendez-vous de test
        if (rendezVousRepository.count() == 0) {
            Utilisateur user = utilisateurRepository.findByEmail("user@erdv.com").orElse(null);
            Prestataire prestataire = prestataireRepository.findAll().get(0);

            if (user != null && prestataire != null) {
                RendezVous rdv = new RendezVous();
                rdv.setUtilisateur(user);
                rdv.setPrestataire(prestataire);
                rdv.setDateHeure(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
                rdv.setService("Consultation générale");
                rdv.setStatut(RendezVous.Statut.EN_ATTENTE);
                rendezVousRepository.save(rdv);
            }
        }
    }
}