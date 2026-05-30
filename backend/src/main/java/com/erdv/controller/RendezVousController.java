package com.erdv.controller;

import com.erdv.dto.CreateRendezVousRequest;
import com.erdv.dto.ReprogrammerRendezVousRequest;
import com.erdv.dto.RendezVousResponse;
import com.erdv.entity.RendezVous;
import com.erdv.entity.Utilisateur;
import com.erdv.service.RendezVousService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

    @GetMapping("/mes-rendez-vous")
    public ResponseEntity<List<RendezVousResponse>> getMesRendezVous() {
        Utilisateur utilisateur = currentUser();
        return ResponseEntity.ok(rendezVousService.getRendezVousByUtilisateur(utilisateur.getId()));
    }

    @GetMapping("/tous")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<RendezVousResponse>> getAllRendezVous(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(rendezVousService.getAllRendezVousPaged(pageable));
    }

    @GetMapping("/mon-agenda")
    @PreAuthorize("hasRole('PRESTATAIRE')")
    public ResponseEntity<List<RendezVousResponse>> getMonAgenda() {
        return ResponseEntity.ok(rendezVousService.getMonAgendaPrestataire(currentUser()));
    }

    @GetMapping("/prestataire/{prestataireId}")
    @PreAuthorize("hasAnyRole('ADMIN','PRESTATAIRE')")
    public ResponseEntity<List<RendezVousResponse>> getRendezVousByPrestataire(@PathVariable Long prestataireId) {
        return ResponseEntity.ok(rendezVousService.getRendezVousByPrestataire(prestataireId));
    }

    @GetMapping("/statut/{statut}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RendezVousResponse>> getRendezVousByStatut(@PathVariable RendezVous.Statut statut) {
        return ResponseEntity.ok(rendezVousService.getRendezVousByStatut(statut));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RendezVousResponse> getRendezVousById(@PathVariable Long id) {
        return ResponseEntity.ok(rendezVousService.getRendezVousById(id, currentUser()));
    }

    @PostMapping
    public ResponseEntity<RendezVousResponse> creerRendezVous(@Valid @RequestBody CreateRendezVousRequest request) {
        return ResponseEntity.ok(rendezVousService.creerRendezVous(currentUser(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RendezVousResponse> updateRendezVous(@PathVariable Long id,
            @Valid @RequestBody RendezVous rendezVous) {
        return ResponseEntity.ok(rendezVousService.updateRendezVous(id, rendezVous, currentUser()));
    }

    @PutMapping("/{id}/confirmer")
    @PreAuthorize("hasAnyRole('ADMIN','PRESTATAIRE')")
    public ResponseEntity<RendezVousResponse> confirmerRendezVous(@PathVariable Long id) {
        return ResponseEntity.ok(rendezVousService.confirmerRendezVous(id, currentUser()));
    }

    @PutMapping("/{id}/annuler")
    public ResponseEntity<RendezVousResponse> annulerRendezVous(@PathVariable Long id) {
        return ResponseEntity.ok(rendezVousService.annulerRendezVous(id, currentUser()));
    }

    @PutMapping("/{id}/reprogrammer")
    public ResponseEntity<RendezVousResponse> reprogrammerRendezVous(
            @PathVariable Long id,
            @Valid @RequestBody ReprogrammerRendezVousRequest request) {
        return ResponseEntity.ok(
                rendezVousService.reprogrammerRendezVous(id, request.getCreneauId(), currentUser()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRendezVous(@PathVariable Long id) {
        rendezVousService.deleteRendezVous(id, currentUser());
        return ResponseEntity.ok().build();
    }

    private static Utilisateur currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Utilisateur) authentication.getPrincipal();
    }
}
