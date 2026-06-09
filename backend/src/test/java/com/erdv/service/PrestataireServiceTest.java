package com.erdv.service;

import com.erdv.dto.PrestataireRequest;
import com.erdv.entity.Etablissement;
import com.erdv.entity.Prestataire;
import com.erdv.exception.ApiException;
import com.erdv.repository.EtablissementRepository;
import com.erdv.repository.PrestataireRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrestataireServiceTest {

    @InjectMocks
    private PrestataireService service;

    @Mock
    private PrestataireRepository prestataireRepository;

    @Mock
    private EtablissementRepository etablissementRepository;

    @Test
    void getAllPrestatairesFiltreParEtablissement() {
        Prestataire p = prestataire(1L);
        when(prestataireRepository.findByEtablissement_IdOrderByNomAsc(3L)).thenReturn(List.of(p));

        assertEquals(1, service.getAllPrestataires(3L).size());
    }

    @Test
    void creerPrestataireExigeEtablissementValide() {
        PrestataireRequest req = request(99L);
        when(etablissementRepository.findById(99L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> service.creerPrestataire(req));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void creerPrestataireSauvegarde() {
        Etablissement etab = new Etablissement();
        etab.setId(1L);
        when(etablissementRepository.findById(1L)).thenReturn(Optional.of(etab));
        when(prestataireRepository.save(any())).thenAnswer(inv -> {
            Prestataire saved = inv.getArgument(0);
            saved.setId(5L);
            return saved;
        });

        assertEquals("Dr Martin", service.creerPrestataire(request(1L)).getNom());
    }

    @Test
    void deletePrestataire() {
        service.deletePrestataire(7L);
        verify(prestataireRepository).deleteById(7L);
    }

    private static PrestataireRequest request(long etabId) {
        PrestataireRequest req = new PrestataireRequest();
        req.setNom("Dr Martin");
        req.setSpecialite("Généraliste");
        req.setEmail("dr@test.com");
        req.setEtablissementId(etabId);
        return req;
    }

    private static Prestataire prestataire(long id) {
        Prestataire p = new Prestataire();
        p.setId(id);
        p.setNom("Dr Martin");
        p.setSpecialite("Généraliste");
        p.setEmail("dr@test.com");
        Etablissement e = new Etablissement();
        e.setId(1L);
        e.setNom("Cabinet");
        p.setEtablissement(e);
        return p;
    }
}
