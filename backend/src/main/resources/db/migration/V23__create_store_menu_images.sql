-- Photos of a restaurant's printed menu card.
--
-- A restaurant's menu already exists on paper; asking the owner to retype it
-- item by item is how a listing stays empty. A photo is one tap and is exactly
-- what a customer would see standing at the counter.
--
-- Stored in Postgres rather than an object store so the app keeps running on
-- the same three containers. The phone resizes before upload, so a page is a
-- few hundred KB, and a restaurant has a handful of pages at most.

CREATE TABLE store_menu_images
(
    id UUID PRIMARY KEY,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    store_id UUID NOT NULL,

    content_type VARCHAR(50) NOT NULL,
    size_bytes INTEGER NOT NULL,

    -- Page order as the owner added them; page 1 is the cover.
    sort_order INTEGER NOT NULL,

    data BYTEA NOT NULL,

    CONSTRAINT fk_store_menu_images_store
        FOREIGN KEY (store_id) REFERENCES stores (id) ON DELETE CASCADE,

    CONSTRAINT chk_store_menu_images_size_positive CHECK (size_bytes > 0)
);

CREATE INDEX idx_store_menu_images_store
    ON store_menu_images (store_id, sort_order);
