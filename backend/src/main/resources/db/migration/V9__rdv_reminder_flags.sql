-- Suivi des rappels e-mail (J-1 et H-2) par rendez-vous
ALTER TABLE rendez_vous
    ADD COLUMN rappel_j1_envoye BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE rendez_vous
    ADD COLUMN rappel_h2_envoye BOOLEAN NOT NULL DEFAULT FALSE;
