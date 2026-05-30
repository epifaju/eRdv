package com.erdv.controller;

import com.erdv.dto.GenerationCreneauxResponse;
import com.erdv.dto.PlageRecurrenteRequest;
import com.erdv.dto.PlageRecurrenteResponse;
import com.erdv.service.PlageRecurrenteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/plages-recurrentes")
public class PlageRecurrenteController {

    @Autowired
    private PlageRecurrenteService plageRecurrenteService;

    @GetMapping("/prestataire/{prestataireId}")
    @PreAuthorize("hasAnyRole('ADMIN','PRESTATAIRE')")
    public ResponseEntity<List<PlageRecurrenteResponse>> listByPrestataire(@PathVariable Long prestataireId) {
        return ResponseEntity.ok(plageRecurrenteService.listByPrestataire(prestataireId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PRESTATAIRE')")
    public ResponseEntity<PlageRecurrenteResponse> creer(@Valid @RequestBody PlageRecurrenteRequest request) {
        return ResponseEntity.ok(plageRecurrenteService.creer(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PRESTATAIRE')")
    public ResponseEntity<PlageRecurrenteResponse> modifier(@PathVariable Long id,
            @Valid @RequestBody PlageRecurrenteRequest request) {
        return ResponseEntity.ok(plageRecurrenteService.modifier(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PRESTATAIRE')")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        plageRecurrenteService.supprimer(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/prestataire/{prestataireId}/generer-creneaux")
    @PreAuthorize("hasAnyRole('ADMIN','PRESTATAIRE')")
    public ResponseEntity<GenerationCreneauxResponse> genererCreneaux(
            @PathVariable Long prestataireId,
            @RequestParam(required = false) Integer jours) {
        return ResponseEntity.ok(plageRecurrenteService.genererCreneaux(prestataireId, jours));
    }
}
