-- Lien compte utilisateur ↔ fiche prestataire (rôle PRESTATAIRE)
ALTER TABLE utilisateurs
    ADD COLUMN IF NOT EXISTS prestataire_id BIGINT REFERENCES prestataires (id) ON DELETE SET NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_utilisateurs_prestataire_id
    ON utilisateurs (prestataire_id)
    WHERE prestataire_id IS NOT NULL;
