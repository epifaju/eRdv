-- Un seul RDV actif (en attente ou confirmé) par créneau
CREATE UNIQUE INDEX IF NOT EXISTS uk_rendez_vous_creneau_actif
    ON rendez_vous (creneau_id)
    WHERE statut IN ('EN_ATTENTE', 'CONFIRME');

CREATE INDEX IF NOT EXISTS idx_creneaux_prestataire_date
    ON creneaux_horaires (prestataire_id, date_heure);
