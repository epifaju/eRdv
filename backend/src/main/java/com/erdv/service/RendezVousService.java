package com.erdv.service;

import com.erdv.dto.CreateRendezVousRequest;
import com.erdv.dto.RendezVousResponse;
import com.erdv.entity.CreneauHoraire;
import com.erdv.entity.RendezVous;
import com.erdv.entity.Utilisateur;
import com.erdv.entity.Prestation;
import com.erdv.exception.ApiException;
import com.erdv.repository.RendezVousRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;

@Service
@Transactional
public class RendezVousService {

    @Autowired
    private RendezVousRepository rendezVousRepository;

    @Autowired
    private CreneauHoraireService creneauHoraireService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PrestationService prestationService;

    @Autowired
    private PrestataireAccessService prestataireAccessService;

    @Transactional(readOnly = true)
    public List<RendezVousResponse> getAllRendezVous() {
        return RendezVousResponse.fromList(rendezVousRepository.findAllWithDetails());
    }

    @Transactional(readOnly = true)
    public Page<RendezVousResponse> getAllRendezVousPaged(Pageable pageable) {
        Page<RendezVous> page = rendezVousRepository.findAllByOrderByDateHeureDesc(pageable);
        if (page.isEmpty()) {
            return Page.empty(pageable);
        }
        List<Long> ids = page.getContent().stream().map(RendezVous::getId).toList();
        List<RendezVousResponse> content = RendezVousResponse.fromList(
                rendezVousRepository.findAllWithDetailsByIds(ids));
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<RendezVousResponse> getRendezVousByUtilisateur(Long utilisateurId) {
        return RendezVousResponse.fromList(rendezVousRepository.findByUtilisateurIdWithDetails(utilisateurId));
    }

    @Transactional(readOnly = true)
    public List<RendezVousResponse> getRendezVousByPrestataire(Long prestataireId) {
        prestataireAccessService.assertCanManagePrestataire(prestataireId);
        return RendezVousResponse.fromList(rendezVousRepository.findByPrestataireIdWithDetails(prestataireId));
    }

    @Transactional(readOnly = true)
    public List<RendezVousResponse> getMonAgendaPrestataire(Utilisateur prestataireUser) {
        if (prestataireUser.getRole() != Utilisateur.Role.PRESTATAIRE
                || prestataireUser.getPrestataire() == null) {
            throw new AccessDeniedException("Réservé aux comptes prestataire");
        }
        return getRendezVousByPrestataire(prestataireUser.getPrestataire().getId());
    }

    @Transactional(readOnly = true)
    public List<RendezVousResponse> getRendezVousByStatut(RendezVous.Statut statut) {
        return RendezVousResponse.fromList(rendezVousRepository.findByStatutWithDetails(statut));
    }

    private RendezVous findByIdOrThrow(Long id) {
        return rendezVousRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Rendez-vous non trouvé"));
    }

    private void assertCanAccess(RendezVous rdv, Utilisateur user) {
        if (user.getRole() == Utilisateur.Role.ADMIN) {
            return;
        }
        if (user.getRole() == Utilisateur.Role.PRESTATAIRE
                && user.getPrestataire() != null
                && rdv.getPrestataire().getId().equals(user.getPrestataire().getId())) {
            return;
        }
        if (rdv.getUtilisateur().getId().equals(user.getId())) {
            return;
        }
        throw new AccessDeniedException("Accès non autorisé à ce rendez-vous");
    }

    private void assertCanManageAsPrestataireOrAdmin(RendezVous rdv, Utilisateur user) {
        if (user.getRole() == Utilisateur.Role.ADMIN) {
            return;
        }
        if (user.getRole() == Utilisateur.Role.PRESTATAIRE
                && user.getPrestataire() != null
                && rdv.getPrestataire().getId().equals(user.getPrestataire().getId())) {
            return;
        }
        throw new AccessDeniedException("Action réservée au prestataire ou à l'administrateur");
    }

    @Transactional(readOnly = true)
    public RendezVousResponse getRendezVousById(Long id, Utilisateur currentUser) {
        RendezVous rdv = findByIdOrThrow(id);
        assertCanAccess(rdv, currentUser);
        return RendezVousResponse.from(rdv);
    }

    public RendezVousResponse creerRendezVous(Utilisateur utilisateur, CreateRendezVousRequest request) {
        Prestation prestation = null;
        String serviceLabel = request.getService();
        int dureeMinutes = 30;

        if (request.getPrestationId() != null) {
            prestation = prestationService.getEntityById(request.getPrestationId());
            if (!prestation.isActif()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Cette prestation n'est plus disponible");
            }
            serviceLabel = prestation.getNom();
            dureeMinutes = prestation.getDureeMinutes();
        }
        if (serviceLabel == null || serviceLabel.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Indiquez une prestation ou décrivez le motif du rendez-vous");
        }

        CreneauHoraire creneauRef = creneauHoraireService.getCreneauById(request.getCreneauId());
        if (prestation != null
                && !prestation.getPrestataire().getId().equals(creneauRef.getPrestataire().getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cette prestation n'appartient pas au prestataire sélectionné");
        }

        int nbSlots = creneauHoraireService.computeSlotsNeeded(dureeMinutes, creneauRef.getDureeMinutes());
        List<CreneauHoraire> slots;
        try {
            slots = creneauHoraireService.reserverCreneauxConsecutifs(request.getCreneauId(), nbSlots);
        } catch (ApiException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new ApiException(HttpStatus.CONFLICT, "Ce créneau vient d'être réservé par quelqu'un d'autre");
        }

        CreneauHoraire creneau = slots.get(0);
        RendezVous rendezVous = new RendezVous();
        rendezVous.setUtilisateur(utilisateur);
        rendezVous.setCreneau(creneau);
        rendezVous.setCreneauxReserves(new ArrayList<>(slots));
        rendezVous.setPrestataire(creneau.getPrestataire());
        rendezVous.setPrestation(prestation);
        rendezVous.setDateHeure(creneau.getDateHeure());
        rendezVous.setService(serviceLabel.trim());
        rendezVous.setStatut(request.getStatut() != null ? request.getStatut() : RendezVous.Statut.EN_ATTENTE);

        RendezVous savedRendezVous;
        try {
            savedRendezVous = rendezVousRepository.save(rendezVous);
        } catch (DataIntegrityViolationException ex) {
            creneauHoraireService.libererCreneauxRendezVous(rendezVous);
            throw new ApiException(HttpStatus.CONFLICT, "Ce créneau vient d'être réservé par quelqu'un d'autre");
        }

        RendezVousResponse response = RendezVousResponse.from(
                rendezVousRepository.findByIdWithDetails(savedRendezVous.getId()).orElse(savedRendezVous));

        try {
            if (savedRendezVous.getStatut() == RendezVous.Statut.CONFIRME) {
                emailService.envoyerConfirmationRendezVous(savedRendezVous);
            } else {
                emailService.envoyerAccuseReceptionRendezVous(savedRendezVous);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de l'email: " + e.getMessage());
        }

        return response;
    }

    public RendezVousResponse updateRendezVous(Long id, RendezVous rendezVousDetails, Utilisateur currentUser) {
        RendezVous rendezVous = findByIdOrThrow(id);
        assertCanAccess(rendezVous, currentUser);

        rendezVous.setDateHeure(rendezVousDetails.getDateHeure());
        rendezVous.setService(rendezVousDetails.getService());
        rendezVous.setStatut(rendezVousDetails.getStatut());

        return RendezVousResponse.from(rendezVousRepository.save(rendezVous));
    }

    public RendezVousResponse confirmerRendezVous(Long id, Utilisateur currentUser) {
        RendezVous rendezVous = findByIdOrThrow(id);
        assertCanManageAsPrestataireOrAdmin(rendezVous, currentUser);
        if (rendezVous.getStatut() == RendezVous.Statut.ANNULE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Impossible de confirmer un rendez-vous annulé");
        }
        rendezVous.setStatut(RendezVous.Statut.CONFIRME);

        RendezVous savedRendezVous = rendezVousRepository.save(rendezVous);

        try {
            emailService.envoyerConfirmationRendezVous(savedRendezVous);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de l'email: " + e.getMessage());
        }

        return RendezVousResponse.from(savedRendezVous);
    }

    public RendezVousResponse annulerRendezVous(Long id, Utilisateur currentUser) {
        RendezVous rendezVous = findByIdOrThrow(id);
        assertCanAccess(rendezVous, currentUser);
        if (rendezVous.getStatut() == RendezVous.Statut.ANNULE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Ce rendez-vous est déjà annulé");
        }
        rendezVous.setStatut(RendezVous.Statut.ANNULE);

        creneauHoraireService.libererCreneauxRendezVous(rendezVous);

        RendezVous savedRendezVous = rendezVousRepository.save(rendezVous);

        try {
            emailService.envoyerAnnulationRendezVous(savedRendezVous);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de l'email: " + e.getMessage());
        }

        return RendezVousResponse.from(savedRendezVous);
    }

    public RendezVousResponse reprogrammerRendezVous(Long id, Long nouveauCreneauId, Utilisateur currentUser) {
        RendezVous rendezVous = findByIdOrThrow(id);
        assertCanAccess(rendezVous, currentUser);

        if (rendezVous.getStatut() == RendezVous.Statut.ANNULE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Impossible de reprogrammer un rendez-vous annulé");
        }
        if (!rendezVous.getDateHeure().isAfter(java.time.LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Impossible de reprogrammer un rendez-vous passé");
        }

        int dureeMinutes = rendezVous.getPrestation() != null
                ? rendezVous.getPrestation().getDureeMinutes()
                : (rendezVous.getCreneauxReserves() != null && !rendezVous.getCreneauxReserves().isEmpty()
                        ? rendezVous.getCreneauxReserves().stream().mapToInt(CreneauHoraire::getDureeMinutes).sum()
                        : rendezVous.getCreneau().getDureeMinutes());

        Long ancienCreneauId = rendezVous.getCreneau().getId();
        if (nouveauCreneauId.equals(ancienCreneauId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Sélectionnez un créneau différent");
        }

        java.time.LocalDateTime ancienneDate = rendezVous.getDateHeure();
        CreneauHoraire creneauRef = creneauHoraireService.getCreneauById(nouveauCreneauId);
        int nbSlots = creneauHoraireService.computeSlotsNeeded(dureeMinutes, creneauRef.getDureeMinutes());
        List<CreneauHoraire> newSlots;
        try {
            newSlots = creneauHoraireService.reserverCreneauxConsecutifs(nouveauCreneauId, nbSlots);
        } catch (ApiException ex) {
            throw ex;
        }

        if (!newSlots.get(0).getPrestataire().getId().equals(rendezVous.getPrestataire().getId())) {
            creneauHoraireService.libererCreneauxRendezVous(buildTempRdv(newSlots));
            throw new ApiException(HttpStatus.BAD_REQUEST, "Ce créneau n'appartient pas au même prestataire");
        }
        if (!newSlots.get(0).getDateHeure().isAfter(java.time.LocalDateTime.now())) {
            creneauHoraireService.libererCreneauxRendezVous(buildTempRdv(newSlots));
            throw new ApiException(HttpStatus.BAD_REQUEST, "Ce créneau est dans le passé");
        }

        creneauHoraireService.libererCreneauxRendezVous(rendezVous);

        rendezVous.setCreneau(newSlots.get(0));
        rendezVous.setCreneauxReserves(new ArrayList<>(newSlots));
        rendezVous.setDateHeure(newSlots.get(0).getDateHeure());

        RendezVous savedRendezVous;
        try {
            savedRendezVous = rendezVousRepository.save(rendezVous);
        } catch (DataIntegrityViolationException ex) {
            creneauHoraireService.libererCreneauxRendezVous(rendezVous);
            throw new ApiException(HttpStatus.CONFLICT, "Ce créneau vient d'être réservé par quelqu'un d'autre");
        }

        RendezVousResponse response = RendezVousResponse.from(
                rendezVousRepository.findByIdWithDetails(savedRendezVous.getId()).orElse(savedRendezVous));

        try {
            emailService.envoyerReprogrammationRendezVous(savedRendezVous, ancienneDate);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de l'email: " + e.getMessage());
        }

        return response;
    }

    public void deleteRendezVous(Long id, Utilisateur currentUser) {
        RendezVous rendezVous = findByIdOrThrow(id);
        assertCanAccess(rendezVous, currentUser);

        if (rendezVous.getStatut() != RendezVous.Statut.ANNULE) {
            creneauHoraireService.libererCreneauxRendezVous(rendezVous);
        }

        rendezVousRepository.deleteById(id);
    }

    private static RendezVous buildTempRdv(List<CreneauHoraire> slots) {
        RendezVous temp = new RendezVous();
        temp.setCreneauxReserves(slots);
        return temp;
    }
}
