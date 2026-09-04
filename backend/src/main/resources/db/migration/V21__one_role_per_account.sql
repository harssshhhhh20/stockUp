-- An account belongs to exactly one side of the marketplace, for good.
--
-- Shopping and shopkeeping were previously two hats on one login: every user
-- was created as a CUSTOMER, and opening a shop layered MERCHANT on top. That
-- made a shopkeeper's Bharosa ambiguous about whose conduct it described, and
-- it made "is this a shopper?" unanswerable — everyone was one.
--
-- Existing accounts are collapsed by what they actually did: anyone who opened
-- a shop is a merchant, everyone else is a shopper. ADMIN is orthogonal and is
-- left untouched.

DELETE FROM user_roles r
WHERE r.role = 'CUSTOMER'
  AND EXISTS (
      SELECT 1 FROM user_roles m
      WHERE m.user_id = r.user_id AND m.role = 'MERCHANT'
  );

-- Guard the invariant in the database, not only in the entity: a stray insert
-- from a script or a future code path must not be able to recreate the state
-- this migration just cleaned up.
CREATE OR REPLACE FUNCTION enforce_single_marketplace_role() RETURNS TRIGGER AS $$
DECLARE
    opposite TEXT := CASE NEW.role WHEN 'CUSTOMER' THEN 'MERCHANT'
                                   WHEN 'MERCHANT' THEN 'CUSTOMER'
                                   ELSE NULL END;
BEGIN
    IF opposite IS NULL THEN
        RETURN NEW;  -- ADMIN and anything like it sits alongside a base role.
    END IF;

    IF EXISTS (
        SELECT 1 FROM user_roles
        WHERE user_id = NEW.user_id AND role = opposite
    ) THEN
        RAISE EXCEPTION
            'User % is already a %; an account cannot be both.',
            NEW.user_id, opposite;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_single_marketplace_role ON user_roles;
CREATE TRIGGER trg_single_marketplace_role
    BEFORE INSERT OR UPDATE ON user_roles
    FOR EACH ROW EXECUTE FUNCTION enforce_single_marketplace_role();
