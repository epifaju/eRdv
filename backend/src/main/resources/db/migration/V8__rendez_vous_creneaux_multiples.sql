-- Créneaux multiples par rendez-vous (prestations longues)
CREATE TABLE rendez_vous_creneaux (
    rendez_vous_id BIGINT NOT NULL REFERENCES rendez_vous (id) ON DELETE CASCADE,
    creneau_id BIGINT NOT NULL REFERENCES creneaux_horaires (id),
    PRIMARY KEY (rendez_vous_id, creneau_id)
);

CREATE UNIQUE INDEX uk_rdv_creneaux_creneau ON rendez_vous_creneaux (creneau_id);

INSERT INTO rendez_vous_creneaux (rendez_vous_id, creneau_id)
SELECT id, creneau_id FROM rendez_vous WHERE creneau_id IS NOT NULL
ON CONFLICT DO NOTHING;
