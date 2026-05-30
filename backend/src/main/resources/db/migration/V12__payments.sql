-- Paiements en ligne (Stripe)

CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    utilisateur_id BIGINT NOT NULL REFERENCES utilisateurs (id),
    rendez_vous_id BIGINT REFERENCES rendez_vous (id),
    prestation_id BIGINT NOT NULL REFERENCES prestations (id),
    creneau_id BIGINT NOT NULL REFERENCES creneaux_horaires (id),
    montant DECIMAL(10, 2) NOT NULL,
    devise VARCHAR(3) NOT NULL DEFAULT 'EUR',
    stripe_payment_intent_id VARCHAR(255) NOT NULL,
    statut VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    service_notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_payments_stripe_intent UNIQUE (stripe_payment_intent_id)
);

CREATE INDEX idx_payments_utilisateur ON payments (utilisateur_id);
CREATE INDEX idx_payments_rendez_vous ON payments (rendez_vous_id);
CREATE INDEX idx_payments_statut ON payments (statut);
