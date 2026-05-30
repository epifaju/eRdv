package com.erdv.service;

import com.erdv.dto.EtablissementRequest;
import com.erdv.dto.EtablissementResponse;
import com.erdv.entity.Etablissement;
import com.erdv.exception.ApiException;
import com.erdv.repository.EtablissementRepository;
import com.erdv.repository.PrestataireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EtablissementService {

    @Autowired
    private EtablissementRepository etablissementRepository;

    @Autowired
    private PrestataireRepository prestataireRepository;

    public List<EtablissementResponse> listPublic() {
        return etablissementRepository.findByActifTrueOrderByNomAsc().stream()
                .map(EtablissementResponse::from)
                .toList();
    }

    public List<EtablissementResponse> listAll() {
        return etablissementRepository.findAll().stream()
                .map(EtablissementResponse::from)
                .toList();
    }

    public EtablissementResponse getByIdPublic(Long id) {
        Etablissement e = getEntityById(id);
        if (!e.isActif()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Établissement non trouvé");
        }
        return EtablissementResponse.from(e);
    }

    public Etablissement getEntityById(Long id) {
        return etablissementRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Établissement non trouvé"));
    }

    @Transactional
    public EtablissementResponse create(EtablissementRequest request) {
        Etablissement e = new Etablissement();
        request.applyTo(e);
        if (request.getActif() == null) {
            e.setActif(true);
        }
        return EtablissementResponse.from(etablissementRepository.save(e));
    }

    @Transactional
    public EtablissementResponse update(Long id, EtablissementRequest request) {
        Etablissement e = getEntityById(id);
        request.applyTo(e);
        return EtablissementResponse.from(etablissementRepository.save(e));
    }

    @Transactional
    public void delete(Long id) {
        Etablissement e = getEntityById(id);
        long count = prestataireRepository.countByEtablissement_Id(id);
        if (count > 0) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Impossible de supprimer un établissement qui a encore des prestataires");
        }
        etablissementRepository.delete(e);
    }
}
