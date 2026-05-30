package com.erdv.service;

import com.erdv.entity.PlageRecurrente;
import com.erdv.entity.Prestation;
import com.erdv.entity.Utilisateur;
import com.erdv.exception.ApiException;
import com.erdv.repository.PlageRecurrenteRepository;
import com.erdv.repository.PrestationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class PrestataireAccessService {

    @Autowired
    private PrestationRepository prestationRepository;

    @Autowired
    private PlageRecurrenteRepository plageRecurrenteRepository;

    public Utilisateur currentUser() {
        return (Utilisateur) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public void assertCanManagePrestataire(Utilisateur user, Long prestataireId) {
        if (user.getRole() == Utilisateur.Role.ADMIN) {
            return;
        }
        if (user.getRole() == Utilisateur.Role.PRESTATAIRE
                && user.getPrestataire() != null
                && user.getPrestataire().getId().equals(prestataireId)) {
            return;
        }
        throw new AccessDeniedException("Accès non autorisé à ce prestataire");
    }

    public void assertCanManagePrestataire(Long prestataireId) {
        assertCanManagePrestataire(currentUser(), prestataireId);
    }

    public void assertCanManagePrestation(Long prestationId) {
        Prestation p = prestationRepository.findById(prestationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Prestation non trouvée"));
        assertCanManagePrestataire(p.getPrestataire().getId());
    }

    public void assertCanManagePlage(Long plageId) {
        PlageRecurrente plage = plageRecurrenteRepository.findById(plageId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Plage horaire non trouvée"));
        assertCanManagePrestataire(plage.getPrestataire().getId());
    }

    /** Pour un PRESTATAIRE : force l'ID lié ; pour ADMIN : accepte la valeur demandée. */
    public Long resolvePrestataireIdForWrite(Utilisateur user, Long requestedPrestataireId) {
        if (user.getRole() == Utilisateur.Role.PRESTATAIRE) {
            if (user.getPrestataire() == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "Compte prestataire non rattaché à une fiche établissement");
            }
            if (requestedPrestataireId != null
                    && !requestedPrestataireId.equals(user.getPrestataire().getId())) {
                throw new AccessDeniedException("Vous ne pouvez gérer que votre propre catalogue");
            }
            return user.getPrestataire().getId();
        }
        if (requestedPrestataireId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "L'ID du prestataire est obligatoire");
        }
        return requestedPrestataireId;
    }
}
