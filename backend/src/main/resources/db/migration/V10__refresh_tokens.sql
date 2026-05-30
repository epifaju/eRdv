CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    jti VARCHAR(64) NOT NULL UNIQUE,
    utilisateur_id BIGINT NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_tokens_utilisateur ON refresh_tokens(utilisateur_id);
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens(expires_at);
