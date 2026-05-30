package com.erdv.controller;

import com.erdv.dto.EtablissementRequest;
import com.erdv.dto.EtablissementResponse;
import com.erdv.dto.PrestataireResponse;
import com.erdv.service.EtablissementService;
import com.erdv.service.PrestataireService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/etablissements")
public class EtablissementController {

    @Autowired
    private EtablissementService etablissementService;

    @Autowired
    private PrestataireService prestataireService;

    @GetMapping
    public ResponseEntity<List<EtablissementResponse>> listPublic() {
        return ResponseEntity.ok(etablissementService.listPublic());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EtablissementResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(etablissementService.getByIdPublic(id));
    }

    @GetMapping("/{id}/prestataires")
    public ResponseEntity<List<PrestataireResponse>> getPrestataires(@PathVariable Long id) {
        etablissementService.getByIdPublic(id);
        return ResponseEntity.ok(prestataireService.getPrestatairesByEtablissement(id));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EtablissementResponse>> listAllAdmin() {
        return ResponseEntity.ok(etablissementService.listAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EtablissementResponse> create(@Valid @RequestBody EtablissementRequest request) {
        return ResponseEntity.ok(etablissementService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EtablissementResponse> update(@PathVariable Long id,
            @Valid @RequestBody EtablissementRequest request) {
        return ResponseEntity.ok(etablissementService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        etablissementService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
