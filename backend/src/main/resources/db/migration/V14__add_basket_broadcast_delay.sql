ALTER TABLE baskets
ADD COLUMN broadcast_at TIMESTAMP;

UPDATE baskets
SET broadcast_at = created_at;

ALTER TABLE baskets
ALTER COLUMN broadcast_at SET NOT NULL;