ALTER TABLE utilisateurs
    ADD COLUMN consentement_sms_rappels BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE utilisateurs
    ADD COLUMN consentement_sms_rappels_at TIMESTAMP;
