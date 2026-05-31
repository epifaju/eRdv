-- Rappels SMS (Twilio) — suivi indépendant des e-mails

ALTER TABLE rendez_vous
    ADD COLUMN rappel_j1_sms_envoye BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE rendez_vous
    ADD COLUMN rappel_h2_sms_envoye BOOLEAN NOT NULL DEFAULT FALSE;
