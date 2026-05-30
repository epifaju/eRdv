-- Multi-établissement (cabinets / sites) + compte utilisateur désactivable (RGPD)

CREATE TABLE etablissements (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    adresse VARCHAR(500),
    ville VARCHAR(100),
    code_postal VARCHAR(20),
    telephone VARCHAR(50),
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

INSERT INTO etablissements (nom, adresse, ville, code_postal, telephone, actif)
VALUES (
    'Cabinet principal',
    '1 rue de la Santé',
    'Paris',
    '75001',
    '0100000000',
    TRUE
);

ALTER TABLE prestataires
    ADD COLUMN etablissement_id BIGINT REFERENCES etablissements (id);

UPDATE prestataires SET etablissement_id = 1 WHERE etablissement_id IS NULL;

ALTER TABLE prestataires
    ALTER COLUMN etablissement_id SET NOT NULL;

ALTER TABLE rendez_vous
    ADD COLUMN etablissement_id BIGINT REFERENCES etablissements (id);

UPDATE rendez_vous rv
SET etablissement_id = p.etablissement_id
FROM prestataires p
WHERE rv.prestataire_id = p.id AND rv.etablissement_id IS NULL;

ALTER TABLE rendez_vous
    ALTER COLUMN etablissement_id SET NOT NULL;

ALTER TABLE utilisateurs
    ADD COLUMN actif BOOLEAN NOT NULL DEFAULT TRUE;

CREATE INDEX idx_prestataires_etablissement ON prestataires (etablissement_id);
CREATE INDEX idx_rendez_vous_etablissement ON rendez_vous (etablissement_id);
