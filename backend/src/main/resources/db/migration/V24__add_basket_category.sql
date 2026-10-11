-- Groceries or food: decides which shops a request reaches.
--
-- Every basket before this was a grocery list, so that is the default, and
-- an app that doesn't send a category keeps working exactly as it did.
ALTER TABLE baskets
    ADD COLUMN category VARCHAR(20) NOT NULL DEFAULT 'GROCERY';
