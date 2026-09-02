-- Devices a user has asked to be notified on.
--
-- A user may have several (phone, tablet, reinstalled app), so this is a list
-- rather than a column on users. Tokens are unique: reinstalling gives the same
-- device a fresh token, and the old one simply stops being registered.

CREATE TABLE push_tokens
(
    id UUID PRIMARY KEY,

    user_id UUID NOT NULL,

    -- Expo push token, e.g. ExponentPushToken[xxxxxxxx]
    token VARCHAR(255) NOT NULL UNIQUE,

    platform VARCHAR(20),

    -- Cleared when Expo tells us the token is dead, so we stop pushing into
    -- the void without deleting the audit trail.
    active BOOLEAN NOT NULL DEFAULT TRUE,
    last_used_at TIMESTAMP WITH TIME ZONE,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

ALTER TABLE push_tokens
    ADD CONSTRAINT fk_push_token_user FOREIGN KEY (user_id) REFERENCES users (id);

CREATE INDEX idx_push_token_user ON push_tokens (user_id, active);
