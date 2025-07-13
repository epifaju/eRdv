package com.erdv.config;

import com.erdv.entity.Utilisateur;
import com.erdv.service.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Créer un utilisateur admin par défaut s'il n'existe pas
        try {
            utilisateurService.getUtilisateurByEmail("admin@erdv.com");
            System.out.println("✅ Utilisateur admin existe déjà");
        } catch (Exception e) {
            // Créer l'utilisateur admin
            Utilisateur admin = new Utilisateur();
            admin.setNom("Administrateur");
            admin.setEmail("admin@erdv.com");
            admin.setTelephone("0123456789");
            admin.setMotDePasse("admin123"); // Le service s'occupera du hashage
            admin.setRole(Utilisateur.Role.ADMIN);

            utilisateurService.creerUtilisateur(admin);
            System.out.println("✅ Utilisateur admin créé avec succès");
            System.out.println("📧 Email: admin@erdv.com");
            System.out.println("🔑 Mot de passe: admin123");
        }
    }
}