CREATE TABLE reservations
(
    id                UUID PRIMARY KEY,
    created_at        TIMESTAMPTZ NOT NULL,
    updated_at        TIMESTAMPTZ NOT NULL,

    basket_id         UUID        NOT NULL,
    merchant_offer_id UUID        NOT NULL,
    customer_id       UUID        NOT NULL,
    store_id          UUID        NOT NULL,

    status            VARCHAR(50) NOT NULL,
    reserved_until    TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_reservation_basket
        FOREIGN KEY (basket_id)
            REFERENCES baskets (id),

    CONSTRAINT fk_reservation_merchant_offer
        FOREIGN KEY (merchant_offer_id)
            REFERENCES merchant_offers (id),

    CONSTRAINT fk_reservation_customer
        FOREIGN KEY (customer_id)
            REFERENCES users (id),

    CONSTRAINT fk_reservation_store
        FOREIGN KEY (store_id)
            REFERENCES stores (id),

    CONSTRAINT uk_reservation_basket
        UNIQUE (basket_id),

    CONSTRAINT uk_reservation_merchant_offer
        UNIQUE (merchant_offer_id)
);