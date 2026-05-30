package com.erdv.controller;

import com.erdv.dto.PrestationRequest;
import com.erdv.dto.PrestationResponse;
import com.erdv.service.PrestationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/prestations")
public class PrestationController {

    @Autowired
    private PrestationService prestationService;

    @GetMapping("/prestataire/{prestataireId}")
    public ResponseEntity<List<PrestationResponse>> listActives(@PathVariable Long prestataireId) {
        return ResponseEntity.ok(prestationService.listActivesByPrestataire(prestataireId));
    }

    @GetMapping("/prestataire/{prestataireId}/toutes")
    @PreAuthorize("hasAnyRole('ADMIN','PRESTATAIRE')")
    public ResponseEntity<List<PrestationResponse>> listAll(@PathVariable Long prestataireId) {
        return ResponseEntity.ok(prestationService.listAllByPrestataire(prestataireId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PRESTATAIRE')")
    public ResponseEntity<PrestationResponse> creer(@Valid @RequestBody PrestationRequest request) {
        return ResponseEntity.ok(prestationService.creer(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PRESTATAIRE')")
    public ResponseEntity<PrestationResponse> modifier(@PathVariable Long id,
            @Valid @RequestBody PrestationRequest request) {
        return ResponseEntity.ok(prestationService.modifier(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PRESTATAIRE')")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        prestationService.supprimer(id);
        return ResponseEntity.ok().build();
    }
}
