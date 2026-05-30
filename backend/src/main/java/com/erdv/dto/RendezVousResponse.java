package com.erdv.dto;

import com.erdv.entity.RendezVous;

import java.time.LocalDateTime;
import java.util.List;

public class RendezVousResponse {

    private Long id;
    private LocalDateTime dateHeure;
    private LocalDateTime dateHeureFin;
    private Integer dureeTotaleMinutes;
    private Integer nbCreneaux;
    private String service;
    private String statut;
    private UtilisateurSummaryResponse utilisateur;
    private PrestataireSummaryResponse prestataire;
    private CreneauSummaryResponse creneau;
    private PrestationResponse prestation;

    public static RendezVousResponse from(RendezVous rdv) {
        if (rdv == null) {
            return null;
        }
        RendezVousResponse r = new RendezVousResponse();
        r.id = rdv.getId();
        r.dateHeure = rdv.getDateHeure();
        r.dureeTotaleMinutes = computeDureeTotale(rdv);
        r.nbCreneaux = computeNbCreneaux(rdv);
        r.dateHeureFin = rdv.getDateHeure().plusMinutes(r.dureeTotaleMinutes);
        r.service = rdv.getService();
        r.statut = rdv.getStatut() != null ? rdv.getStatut().name() : null;
        r.utilisateur = UtilisateurSummaryResponse.from(rdv.getUtilisateur());
        r.prestataire = PrestataireSummaryResponse.from(rdv.getPrestataire());
        r.creneau = CreneauSummaryResponse.from(rdv.getCreneau());
        r.prestation = PrestationResponse.from(rdv.getPrestation());
        return r;
    }

    public static List<RendezVousResponse> fromList(List<RendezVous> list) {
        return list.stream().map(RendezVousResponse::from).toList();
    }

    private static int computeDureeTotale(com.erdv.entity.RendezVous rdv) {
        if (rdv.getPrestation() != null && rdv.getPrestation().getDureeMinutes() != null) {
            return rdv.getPrestation().getDureeMinutes();
        }
        if (rdv.getCreneauxReserves() != null && !rdv.getCreneauxReserves().isEmpty()) {
            return rdv.getCreneauxReserves().stream().mapToInt(c -> c.getDureeMinutes()).sum();
        }
        if (rdv.getCreneau() != null) {
            return rdv.getCreneau().getDureeMinutes();
        }
        return 30;
    }

    private static int computeNbCreneaux(com.erdv.entity.RendezVous rdv) {
        if (rdv.getCreneauxReserves() != null && !rdv.getCreneauxReserves().isEmpty()) {
            return rdv.getCreneauxReserves().size();
        }
        return 1;
    }

    public LocalDateTime getDateHeureFin() {
        return dateHeureFin;
    }

    public void setDateHeureFin(LocalDateTime dateHeureFin) {
        this.dateHeureFin = dateHeureFin;
    }

    public Integer getDureeTotaleMinutes() {
        return dureeTotaleMinutes;
    }

    public void setDureeTotaleMinutes(Integer dureeTotaleMinutes) {
        this.dureeTotaleMinutes = dureeTotaleMinutes;
    }

    public Integer getNbCreneaux() {
        return nbCreneaux;
    }

    public void setNbCreneaux(Integer nbCreneaux) {
        this.nbCreneaux = nbCreneaux;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDateHeure() {
        return dateHeure;
    }

    public void setDateHeure(LocalDateTime dateHeure) {
        this.dateHeure = dateHeure;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public UtilisateurSummaryResponse getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(UtilisateurSummaryResponse utilisateur) {
        this.utilisateur = utilisateur;
    }

    public PrestataireSummaryResponse getPrestataire() {
        return prestataire;
    }

    public void setPrestataire(PrestataireSummaryResponse prestataire) {
        this.prestataire = prestataire;
    }

    public CreneauSummaryResponse getCreneau() {
        return creneau;
    }

    public void setCreneau(CreneauSummaryResponse creneau) {
        this.creneau = creneau;
    }

    public PrestationResponse getPrestation() {
        return prestation;
    }

    public void setPrestation(PrestationResponse prestation) {
        this.prestation = prestation;
    }
}
