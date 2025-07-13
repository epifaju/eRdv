package com.erdv.controller;

import com.erdv.entity.Prestataire;
import com.erdv.service.PrestataireService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/prestataires")
public class PrestataireController {

    @Autowired
    private PrestataireService prestataireService;

    @GetMapping
    public ResponseEntity<List<Prestataire>> getAllPrestataires() {
        return ResponseEntity.ok(prestataireService.getAllPrestataires());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Prestataire> getPrestataireById(@PathVariable Long id) {
        return ResponseEntity.ok(prestataireService.getPrestataireById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Prestataire> creerPrestataire(@Valid @RequestBody Prestataire prestataire) {
        return ResponseEntity.ok(prestataireService.creerPrestataire(prestataire));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Prestataire> updatePrestataire(@PathVariable Long id,
            @Valid @RequestBody Prestataire prestataire) {
        return ResponseEntity.ok(prestataireService.updatePrestataire(id, prestataire));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePrestataire(@PathVariable Long id) {
        prestataireService.deletePrestataire(id);
        return ResponseEntity.ok().build();
    }
}