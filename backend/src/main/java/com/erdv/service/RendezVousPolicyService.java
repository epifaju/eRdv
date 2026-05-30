package com.erdv.service;

import com.erdv.dto.RendezVousResponse;
import com.erdv.entity.RendezVous;
import com.erdv.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RendezVousPolicyService {

    @Value("${app.rdv.delai-annulation-heures:24}")
    private int delaiAnnulationHeures;

    public int getDelaiAnnulationHeures() {
        return delaiAnnulationHeures;
    }

    public boolean peutAnnulerParClient(RendezVous rdv) {
        if (rdv == null || rdv.getStatut() == null || rdv.getDateHeure() == null) {
            return false;
        }
        if (rdv.getStatut() == RendezVous.Statut.ANNULE) {
            return false;
        }
        if (rdv.getStatut() != RendezVous.Statut.EN_ATTENTE
                && rdv.getStatut() != RendezVous.Statut.CONFIRME) {
            return false;
        }
        if (!rdv.getDateHeure().isAfter(LocalDateTime.now())) {
            return false;
        }
        LocalDateTime limiteAnnulation = rdv.getDateHeure().minusHours(delaiAnnulationHeures);
        return !LocalDateTime.now().isAfter(limiteAnnulation);
    }

    public void assertClientPeutAnnuler(RendezVous rdv) {
        if (peutAnnulerParClient(rdv)) {
            return;
        }
        if (rdv.getStatut() == RendezVous.Statut.ANNULE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Ce rendez-vous est déjà annulé");
        }
        if (!rdv.getDateHeure().isAfter(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Impossible d'annuler un rendez-vous passé");
        }
        throw new ApiException(
                HttpStatus.CONFLICT,
                "Annulation impossible : vous devez annuler au moins "
                        + delaiAnnulationHeures
                        + " h avant le rendez-vous. Contactez le prestataire.");
    }

    public void enrichResponse(RendezVousResponse response, RendezVous rdv) {
        if (response == null) {
            return;
        }
        response.setDelaiAnnulationHeures(delaiAnnulationHeures);
        response.setAnnulableParClient(peutAnnulerParClient(rdv));
    }
}
