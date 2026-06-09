package com.erdv.service;

import com.erdv.dto.EtablissementRequest;
import com.erdv.entity.Etablissement;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EtablissementServiceTest {

    @InjectMocks
    private EtablissementService service;

    @Mock
    private EtablissementRepository etablissementRepository;

    @Mock
    private PrestataireRepository prestataireRepository;

    @Test
    void listPublicRetourneEtablissementsActifs() {
        Etablissement e = etablissement(1L, "Cabinet", true);
        when(etablissementRepository.findByActifTrueOrderByNomAsc()).thenReturn(List.of(e));

        assertEquals(1, service.listPublic().size());
        assertEquals("Cabinet", service.listPublic().get(0).getNom());
    }

    @Test
    void getByIdPublicRefuseInactif() {
        when(etablissementRepository.findById(1L)).thenReturn(Optional.of(etablissement(1L, "X", false)));

        ApiException ex = assertThrows(ApiException.class, () -> service.getByIdPublic(1L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void deleteRefuseSiPrestatairesLies() {
        Etablissement e = etablissement(1L, "Cabinet", true);
        when(etablissementRepository.findById(1L)).thenReturn(Optional.of(e));
        when(prestataireRepository.countByEtablissement_Id(1L)).thenReturn(2L);

        ApiException ex = assertThrows(ApiException.class, () -> service.delete(1L));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(etablissementRepository, never()).delete(any());
    }

    @Test
    void createAppliqueRequest() {
        EtablissementRequest req = new EtablissementRequest();
        req.setNom("Nouveau");
        req.setVille("Lyon");
        when(etablissementRepository.save(any())).thenAnswer(inv -> {
            Etablissement saved = inv.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        assertEquals("Nouveau", service.create(req).getNom());
    }

    private static Etablissement etablissement(long id, String nom, boolean actif) {
        Etablissement e = new Etablissement();
        e.setId(id);
        e.setNom(nom);
        e.setActif(actif);
        return e;
    }
}
