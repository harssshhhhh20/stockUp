-- Phone uniqueness is per-role, not global.
--
-- One household phone legitimately backs two accounts: the shopkeeper's own
-- shop account, and a personal shopping account. What must never happen is two
-- shopkeepers, or two shoppers, claiming the same number — that is how one
-- person farms Bharosa across sockpuppets, and how a stranger hijacks the
-- number a shop is reached on.
--
-- Roles live in user_roles, and Postgres cannot reference another table from a
-- partial index predicate, so we carry a mirrored flag on users and keep it
-- true by trigger rather than by application discipline.

ALTER TABLE users DROP CONSTRAINT IF EXISTS users_phone_number_key;
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_phone_key;

ALTER TABLE users ADD COLUMN IF NOT EXISTS is_merchant BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE users u
SET is_merchant = EXISTS (
    SELECT 1 FROM user_roles r
    WHERE r.user_id = u.id AND r.role = 'MERCHANT'
);

CREATE OR REPLACE FUNCTION sync_user_is_merchant() RETURNS TRIGGER AS $$
DECLARE
    target UUID := COALESCE(NEW.user_id, OLD.user_id);
BEGIN
    UPDATE users u
    SET is_merchant = EXISTS (
        SELECT 1 FROM user_roles r
        WHERE r.user_id = target AND r.role = 'MERCHANT'
    )
    WHERE u.id = target;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_sync_user_is_merchant ON user_roles;
CREATE TRIGGER trg_sync_user_is_merchant
    AFTER INSERT OR UPDATE OR DELETE ON user_roles
    FOR EACH ROW EXECUTE FUNCTION sync_user_is_merchant();

-- Two partial indexes rather than one on (phone, is_merchant): NULL phones are
-- excluded outright, so a half-finished profile never blocks anyone.
CREATE UNIQUE INDEX IF NOT EXISTS ux_users_phone_merchant
    ON users (phone) WHERE is_merchant AND phone IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS ux_users_phone_customer
    ON users (phone) WHERE NOT is_merchant AND phone IS NOT NULL;
