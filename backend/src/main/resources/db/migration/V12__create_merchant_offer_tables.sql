CREATE TABLE merchant_offers
(
    id                     UUID PRIMARY KEY,
    created_at             TIMESTAMPTZ NOT NULL,
    updated_at             TIMESTAMPTZ NOT NULL,

    broadcast_recipient_id UUID        NOT NULL,
    status                 VARCHAR(50) NOT NULL,

    CONSTRAINT fk_merchant_offer_broadcast_recipient
        FOREIGN KEY (broadcast_recipient_id)
            REFERENCES broadcast_recipients (id),

    CONSTRAINT uk_merchant_offer_broadcast_recipient
        UNIQUE (broadcast_recipient_id)
);

CREATE TABLE merchant_offer_items
(
    id                 UUID PRIMARY KEY,
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ NOT NULL,

    merchant_offer_id  UUID           NOT NULL,
    basket_item_id     UUID           NOT NULL,

    status             VARCHAR(50)    NOT NULL,
    available_quantity NUMERIC(19, 2),

    CONSTRAINT fk_offer_item_offer
        FOREIGN KEY (merchant_offer_id)
            REFERENCES merchant_offers (id),

    CONSTRAINT fk_offer_item_basket_item
        FOREIGN KEY (basket_item_id)
            REFERENCES basket_items (id),

    CONSTRAINT uk_offer_item_offer_basket_item
        UNIQUE (merchant_offer_id, basket_item_id)
);