package com.erdv.controller;

import com.erdv.dto.CreateRendezVousRequest;
import com.erdv.entity.RendezVous;
import com.erdv.entity.Utilisateur;
import com.erdv.entity.Prestataire;
import com.erdv.service.RendezVousService;
import com.erdv.service.UtilisateurService;
import com.erdv.service.PrestataireService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rendez-vous")
public class RendezVousController {

    @Autowired
    private RendezVousService rendezVousService;

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private PrestataireService prestataireService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RendezVous>> getAllRendezVous() {
        return ResponseEntity.ok(rendezVousService.getAllRendezVous());
    }

    @GetMapping("/mes-rendez-vous")
    public ResponseEntity<List<RendezVous>> getMesRendezVous() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return ResponseEntity.ok(rendezVousService.getRendezVousByUtilisateur(utilisateur.getId()));
    }

    @GetMapping("/prestataire/{prestataireId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RendezVous>> getRendezVousByPrestataire(@PathVariable Long prestataireId) {
        return ResponseEntity.ok(rendezVousService.getRendezVousByPrestataire(prestataireId));
    }

    @GetMapping("/statut/{statut}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RendezVous>> getRendezVousByStatut(@PathVariable RendezVous.Statut statut) {
        return ResponseEntity.ok(rendezVousService.getRendezVousByStatut(statut));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RendezVous> getRendezVousById(@PathVariable Long id) {
        return ResponseEntity.ok(rendezVousService.getRendezVousById(id));
    }

    @PostMapping
    public ResponseEntity<RendezVous> creerRendezVous(@Valid @RequestBody CreateRendezVousRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();

        // Récupérer le prestataire par son ID
        Prestataire prestataire = prestataireService.getPrestataireById(request.getPrestataireId());

        // Créer le rendez-vous
        RendezVous rendezVous = new RendezVous();
        rendezVous.setUtilisateur(utilisateur);
        rendezVous.setPrestataire(prestataire);
        rendezVous.setDateHeure(request.getDateHeure());
        rendezVous.setService(request.getService());
        rendezVous.setStatut(request.getStatut());

        return ResponseEntity.ok(rendezVousService.creerRendezVous(rendezVous));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RendezVous> updateRendezVous(@PathVariable Long id,
            @Valid @RequestBody RendezVous rendezVous) {
        return ResponseEntity.ok(rendezVousService.updateRendezVous(id, rendezVous));
    }

    @PutMapping("/{id}/confirmer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RendezVous> confirmerRendezVous(@PathVariable Long id) {
        return ResponseEntity.ok(rendezVousService.confirmerRendezVous(id));
    }

    @PutMapping("/{id}/annuler")
    public ResponseEntity<RendezVous> annulerRendezVous(@PathVariable Long id) {
        return ResponseEntity.ok(rendezVousService.annulerRendezVous(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRendezVous(@PathVariable Long id) {
        rendezVousService.deleteRendezVous(id);
        return ResponseEntity.ok().build();
    }
}