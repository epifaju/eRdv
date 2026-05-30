-- Schéma initial aligné sur les entités JPA (PostgreSQL)

CREATE TABLE utilisateurs (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    telephone VARCHAR(255) NOT NULL,
    mot_de_passe VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL
);

CREATE TABLE prestataires (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    specialite VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE creneaux_horaires (
    id BIGSERIAL PRIMARY KEY,
    prestataire_id BIGINT NOT NULL REFERENCES prestataires (id),
    date_heure TIMESTAMP NOT NULL,
    disponible BOOLEAN NOT NULL
);

CREATE TABLE rendez_vous (
    id BIGSERIAL PRIMARY KEY,
    utilisateur_id BIGINT NOT NULL REFERENCES utilisateurs (id),
    prestataire_id BIGINT NOT NULL REFERENCES prestataires (id),
    creneau_id BIGINT NOT NULL REFERENCES creneaux_horaires (id),
    date_heure TIMESTAMP NOT NULL,
    service VARCHAR(255) NOT NULL,
    statut VARCHAR(255) NOT NULL
);
