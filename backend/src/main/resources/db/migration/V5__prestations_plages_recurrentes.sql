-- Catalogue de prestations et plages horaires récurrentes (V2)

CREATE TABLE prestations (
    id BIGSERIAL PRIMARY KEY,
    prestataire_id BIGINT NOT NULL REFERENCES prestataires (id) ON DELETE CASCADE,
    nom VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    duree_minutes INT NOT NULL DEFAULT 30,
    prix DECIMAL(10, 2),
    actif BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_prestations_prestataire ON prestations (prestataire_id);

CREATE TABLE plages_recurrentes (
    id BIGSERIAL PRIMARY KEY,
    prestataire_id BIGINT NOT NULL REFERENCES prestataires (id) ON DELETE CASCADE,
    -- ISO-8601 : 1 = lundi … 7 = dimanche
    jour_semaine SMALLINT NOT NULL CHECK (jour_semaine BETWEEN 1 AND 7),
    heure_debut TIME NOT NULL,
    heure_fin TIME NOT NULL,
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_plage_heures CHECK (heure_fin > heure_debut)
);

CREATE INDEX idx_plages_prestataire ON plages_recurrentes (prestataire_id);

ALTER TABLE creneaux_horaires
    ADD COLUMN IF NOT EXISTS duree_minutes INT NOT NULL DEFAULT 30;

ALTER TABLE rendez_vous
    ADD COLUMN IF NOT EXISTS prestation_id BIGINT REFERENCES prestations (id);
