package com.erdv.config;

import com.erdv.entity.Utilisateur;
import com.erdv.entity.Prestataire;
import com.erdv.entity.Etablissement;
import com.erdv.entity.CreneauHoraire;
import com.erdv.entity.RendezVous;
import com.erdv.entity.Prestation;
import com.erdv.entity.PlageRecurrente;
import com.erdv.repository.UtilisateurRepository;
import com.erdv.repository.PrestataireRepository;
import com.erdv.repository.EtablissementRepository;
import com.erdv.repository.CreneauHoraireRepository;
import com.erdv.repository.RendezVousRepository;
import com.erdv.repository.PrestationRepository;
import com.erdv.repository.PlageRecurrenteRepository;
import com.erdv.service.CreneauGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalTime;

@Component
@ConditionalOnProperty(name = "app.seed-demo-users", havingValue = "true")
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PrestataireRepository prestataireRepository;

    @Autowired
    private EtablissementRepository etablissementRepository;

    @Autowired
    private CreneauHoraireRepository creneauHoraireRepository;

    @Autowired
    private RendezVousRepository rendezVousRepository;

    @Autowired
    private PrestationRepository prestationRepository;

    @Autowired
    private PlageRecurrenteRepository plageRecurrenteRepository;

    @Autowired
    private CreneauGenerationService creneauGenerationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        utilisateurRepository.findByEmail("admin@erdv.com").ifPresentOrElse(
                u -> {
                    if (u.getRole() != Utilisateur.Role.ADMIN) {
                        u.setRole(Utilisateur.Role.ADMIN);
                        u.setMotDePasse(passwordEncoder.encode("admin123"));
                        utilisateurRepository.save(u);
                    }
                },
                () -> {
                    Utilisateur admin = new Utilisateur();
                    admin.setNom("Administrateur");
                    admin.setEmail("admin@erdv.com");
                    admin.setTelephone("0123456789");
                    admin.setMotDePasse(passwordEncoder.encode("admin123"));
                    admin.setRole(Utilisateur.Role.ADMIN);
                    utilisateurRepository.save(admin);
                });

        if (utilisateurRepository.findByEmail("user@erdv.com").isEmpty()) {
            Utilisateur user = new Utilisateur();
            user.setNom("Utilisateur Test");
            user.setEmail("user@erdv.com");
            user.setTelephone("0987654321");
            user.setMotDePasse(passwordEncoder.encode("user123"));
            user.setRole(Utilisateur.Role.USER);
            utilisateurRepository.save(user);
        }

        if (prestataireRepository.count() == 0) {
            Etablissement paris = seedEtablissement(
                    "Cabinet Paris Centre",
                    "12 avenue de la République",
                    "Paris",
                    "75011",
                    "0142000000");
            Etablissement lyon = seedEtablissement(
                    "Centre Santé Lyon",
                    "5 place Bellecour",
                    "Lyon",
                    "69002",
                    "0472000000");
            seedPrestataire("Dr. Martin", "Médecin généraliste", "martin@erdv.com", paris);
            seedPrestataire("Dr. Dubois", "Dentiste", "dubois@erdv.com", paris);
            seedPrestataire("Mme. Laurent", "Kinésithérapeute", "laurent@erdv.com", lyon);
        }

        seedCatalogueEtPlages();
        seedComptesPrestataires();
        creneauGenerationService.genererPourTousLesPrestataires();

        if (rendezVousRepository.count() == 0) {
            Utilisateur user = utilisateurRepository.findByEmail("user@erdv.com").orElse(null);
            Prestataire prestataire = prestataireRepository.findAll().get(0);
            Prestation prestation = prestationRepository.findByPrestataireIdAndActifTrueOrderByNomAsc(prestataire.getId())
                    .stream().findFirst().orElse(null);

            if (user != null && prestataire != null) {
                creneauHoraireRepository.findByPrestataireIdAndDisponibleTrueOrderByDateHeure(prestataire.getId())
                        .stream()
                        .findFirst()
                        .ifPresent(creneau -> {
                            creneau.setDisponible(false);
                            creneauHoraireRepository.save(creneau);

                            RendezVous rdv = new RendezVous();
                            rdv.setUtilisateur(user);
                            rdv.setPrestataire(prestataire);
                            rdv.setEtablissement(prestataire.getEtablissement());
                            rdv.setCreneau(creneau);
                            rdv.setPrestation(prestation);
                            rdv.setDateHeure(creneau.getDateHeure());
                            rdv.setService(prestation != null ? prestation.getNom() : "Consultation générale");
                            rdv.setStatut(RendezVous.Statut.EN_ATTENTE);
                            rendezVousRepository.save(rdv);
                        });
            }
        }
    }

    private Etablissement seedEtablissement(String nom, String adresse, String ville, String cp, String tel) {
        Etablissement e = new Etablissement();
        e.setNom(nom);
        e.setAdresse(adresse);
        e.setVille(ville);
        e.setCodePostal(cp);
        e.setTelephone(tel);
        e.setActif(true);
        return etablissementRepository.save(e);
    }

    private void seedPrestataire(String nom, String specialite, String email, Etablissement etablissement) {
        Prestataire p = new Prestataire();
        p.setNom(nom);
        p.setSpecialite(specialite);
        p.setEmail(email);
        p.setEtablissement(etablissement);
        prestataireRepository.save(p);
    }

    /** Comptes PRESTATAIRE liés aux fiches démo (email = login). */
    private void seedComptesPrestataires() {
        linkPrestataireUser("martin@erdv.com", "prestataire123", "martin@erdv.com", "Dr. Martin");
        linkPrestataireUser("dubois@erdv.com", "prestataire123", "dubois@erdv.com", "Dr. Dubois");
        linkPrestataireUser("laurent@erdv.com", "prestataire123", "laurent@erdv.com", "Mme. Laurent");
    }

    private void linkPrestataireUser(String userEmail, String password, String prestataireEmail, String nom) {
        prestataireRepository.findByEmail(prestataireEmail).ifPresent(prestataire -> {
            utilisateurRepository.findByEmail(userEmail).ifPresentOrElse(
                    user -> {
                        if (user.getRole() != Utilisateur.Role.PRESTATAIRE
                                || user.getPrestataire() == null
                                || !user.getPrestataire().getId().equals(prestataire.getId())) {
                            user.setRole(Utilisateur.Role.PRESTATAIRE);
                            user.setPrestataire(prestataire);
                            user.setNom(nom);
                            utilisateurRepository.save(user);
                        }
                    },
                    () -> {
                        Utilisateur user = new Utilisateur();
                        user.setNom(nom);
                        user.setEmail(userEmail);
                        user.setTelephone("0100000000");
                        user.setMotDePasse(passwordEncoder.encode(password));
                        user.setRole(Utilisateur.Role.PRESTATAIRE);
                        user.setPrestataire(prestataire);
                        utilisateurRepository.save(user);
                    });
        });
    }

    private void seedCatalogueEtPlages() {
        for (Prestataire p : prestataireRepository.findAll()) {
            if (prestationRepository.findByPrestataireIdOrderByNomAsc(p.getId()).isEmpty()) {
                seedPrestationsDemo(p);
            }
            if (plageRecurrenteRepository.findByPrestataireIdOrderByJourSemaineAscHeureDebutAsc(p.getId()).isEmpty()) {
                seedPlagesSemaine(p);
            }
        }
    }

    private void seedPrestationsDemo(Prestataire p) {
        String spec = p.getSpecialite().toLowerCase();
        if (spec.contains("dent")) {
            addPrestation(p, "Consultation dentaire", "Bilan et diagnostic", 30, "45.00");
            addPrestation(p, "Détartrage", "Soin complet", 45, "70.00");
        } else if (spec.contains("kiné")) {
            addPrestation(p, "Séance de rééducation", "30 minutes", 30, "35.00");
            addPrestation(p, "Bilan kinésithérapique", "Première consultation", 45, "50.00");
        } else if (spec.contains("médecin") || spec.contains("general")) {
            addPrestation(p, "Consultation générale", "Médecine générale", 20, "25.00");
            addPrestation(p, "Consultation longue", "Suivi pathologie chronique", 40, "45.00");
        } else {
            addPrestation(p, "Consultation standard", "Prestation de base", 30, null);
            addPrestation(p, "Consultation approfondie", "Prestation longue", 60, null);
        }
    }

    private void addPrestation(Prestataire p, String nom, String desc, int duree, String prix) {
        Prestation prest = new Prestation();
        prest.setPrestataire(p);
        prest.setNom(nom);
        prest.setDescription(desc);
        prest.setDureeMinutes(duree);
        if (prix != null) {
            prest.setPrix(new BigDecimal(prix));
        }
        prest.setActif(true);
        prestationRepository.save(prest);
    }

    /** Lun–ven 9h–12h et 14h–18h (adaptable à tout type d'activité). */
    private void seedPlagesSemaine(Prestataire p) {
        for (int jour = 1; jour <= 5; jour++) {
            addPlage(p, jour, LocalTime.of(9, 0), LocalTime.of(12, 0));
            addPlage(p, jour, LocalTime.of(14, 0), LocalTime.of(18, 0));
        }
    }

    private void addPlage(Prestataire p, int jour, LocalTime debut, LocalTime fin) {
        PlageRecurrente plage = new PlageRecurrente();
        plage.setPrestataire(p);
        plage.setJourSemaine(jour);
        plage.setHeureDebut(debut);
        plage.setHeureFin(fin);
        plage.setActif(true);
        plageRecurrenteRepository.save(plage);
    }
}
