package com.erdv.controller;

import com.erdv.entity.CreneauHoraire;
import com.erdv.service.CreneauHoraireService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/creneaux")
public class CreneauHoraireController {

    @Autowired
    private CreneauHoraireService creneauHoraireService;

    @GetMapping
    public ResponseEntity<List<CreneauHoraire>> getAllCreneaux() {
        return ResponseEntity.ok(creneauHoraireService.getAllCreneaux());
    }

    @GetMapping("/prestataire/{prestataireId}")
    public ResponseEntity<List<CreneauHoraire>> getCreneauxByPrestataire(@PathVariable Long prestataireId) {
        return ResponseEntity.ok(creneauHoraireService.getCreneauxByPrestataire(prestataireId));
    }

    @GetMapping("/prestataire/{prestataireId}/disponibles")
    public ResponseEntity<List<CreneauHoraire>> getCreneauxDisponibles(@PathVariable Long prestataireId) {
        return ResponseEntity.ok(creneauHoraireService.getCreneauxDisponibles(prestataireId));
    }

    @GetMapping("/prestataire/{prestataireId}/disponibles/date")
    public ResponseEntity<List<CreneauHoraire>> getCreneauxDisponiblesForDate(
            @PathVariable Long prestataireId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
            @RequestParam(required = false) Integer dureeMinutes) {
        if (date != null) {
            return ResponseEntity.ok(
                    creneauHoraireService.getCreneauxDisponiblesForDate(prestataireId, date, dureeMinutes));
        }
        if (dateDebut != null) {
            return ResponseEntity.ok(creneauHoraireService.getCreneauxDisponibles(prestataireId, dateDebut));
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CreneauHoraire> getCreneauById(@PathVariable Long id) {
        return ResponseEntity.ok(creneauHoraireService.getCreneauById(id));
    }

    @PostMapping
    public ResponseEntity<CreneauHoraire> creerCreneau(@Valid @RequestBody CreneauHoraire creneau) {
        return ResponseEntity.ok(creneauHoraireService.creerCreneau(creneau));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CreneauHoraire> updateCreneau(@PathVariable Long id,
            @Valid @RequestBody CreneauHoraire creneau) {
        return ResponseEntity.ok(creneauHoraireService.updateCreneau(id, creneau));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCreneau(@PathVariable Long id) {
        creneauHoraireService.deleteCreneau(id);
        return ResponseEntity.ok().build();
    }
}