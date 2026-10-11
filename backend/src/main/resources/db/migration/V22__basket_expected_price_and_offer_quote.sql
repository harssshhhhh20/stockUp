-- Money enters StockUp, without asking a shopkeeper to type it.
--
-- The shopper says what they expect to pay per unit. Every price the shop then
-- quotes is remembered, so the next request for the same product arrives
-- already filled in. A shopkeeper mid-rush corrects an outlier or two and
-- sends; he never faces an empty column. That is the only way per-item prices
-- and reply speed can both survive.
--
-- Every price column is nullable. A shopper who doesn't know still gets a
-- reply, and a shop that only wants to answer availability still can.

ALTER TABLE basket_items
    ADD COLUMN expected_unit_price NUMERIC(10, 2);

ALTER TABLE merchant_offer_items
    ADD COLUMN quoted_unit_price NUMERIC(10, 2);

ALTER TABLE merchant_offers
    ADD COLUMN quoted_total NUMERIC(10, 2);

ALTER TABLE basket_items
    ADD CONSTRAINT chk_basket_items_expected_price_positive
    CHECK (expected_unit_price IS NULL OR expected_unit_price > 0);

ALTER TABLE merchant_offer_items
    ADD CONSTRAINT chk_offer_items_quoted_price_positive
    CHECK (quoted_unit_price IS NULL OR quoted_unit_price > 0);

ALTER TABLE merchant_offers
    ADD CONSTRAINT chk_merchant_offers_quoted_total_positive
    CHECK (quoted_total IS NULL OR quoted_total >= 0);

-- The passive price book: what this shop last charged for this thing.
--
-- Built entirely as a side effect of replying, so there is no catalogue to set
-- up and nothing to maintain. product_key is a normalised product name because
-- StockUp has no catalogue and basket items are free text — "Toor Dal 1kg" and
-- "toor dal  1kg" have to land on the same row or the memory never warms up.
CREATE TABLE store_item_prices
(
    id UUID PRIMARY KEY,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    store_id UUID NOT NULL,
    product_key VARCHAR(160) NOT NULL,
    unit VARCHAR(30) NOT NULL,

    unit_price NUMERIC(10, 2) NOT NULL,
    times_quoted INTEGER NOT NULL DEFAULT 1,

    CONSTRAINT fk_store_item_prices_store
        FOREIGN KEY (store_id) REFERENCES stores(id) ON DELETE CASCADE,

    CONSTRAINT chk_store_item_prices_positive CHECK (unit_price > 0),

    -- One remembered price per product per unit per shop.
    CONSTRAINT ux_store_item_prices UNIQUE (store_id, product_key, unit)
);

CREATE INDEX idx_store_item_prices_lookup
    ON store_item_prices (store_id, product_key);
