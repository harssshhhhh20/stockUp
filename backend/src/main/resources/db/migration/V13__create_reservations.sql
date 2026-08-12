CREATE TABLE reservations
(
    id UUID PRIMARY KEY,

    basket_id UUID NOT NULL,
    merchant_offer_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    merchant_id UUID NOT NULL,
    store_id UUID NOT NULL,

    status VARCHAR(50) NOT NULL,

    notification_sent_at TIMESTAMP WITH TIME ZONE,
    viewed_at TIMESTAMP WITH TIME ZONE,
    active_at TIMESTAMP WITH TIME ZONE,

    cancellation_reason VARCHAR(500),
    otp VARCHAR(6),

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

ALTER TABLE reservations
    ADD CONSTRAINT fk_reservation_basket
        FOREIGN KEY (basket_id)
            REFERENCES baskets (id);

ALTER TABLE reservations
    ADD CONSTRAINT fk_reservation_merchant_offer
        FOREIGN KEY (merchant_offer_id)
            REFERENCES merchant_offers (id);

ALTER TABLE reservations
    ADD CONSTRAINT fk_reservation_customer
        FOREIGN KEY (customer_id)
            REFERENCES users (id);

ALTER TABLE reservations
    ADD CONSTRAINT fk_reservation_merchant
        FOREIGN KEY (merchant_id)
            REFERENCES merchants (id);

ALTER TABLE reservations
    ADD CONSTRAINT fk_reservation_store
        FOREIGN KEY (store_id)
            REFERENCES stores (id);

ALTER TABLE reservations
    ADD CONSTRAINT uk_reservation_basket
        UNIQUE (basket_id);

ALTER TABLE reservations
    ADD CONSTRAINT uk_reservation_merchant_offer
        UNIQUE (merchant_offer_id);

CREATE INDEX idx_reservation_status_created_at
    ON reservations (status, created_at);

CREATE INDEX idx_reservation_status_active_at
    ON reservations (status, active_at);

CREATE INDEX idx_reservation_customer
    ON reservations (customer_id);

CREATE INDEX idx_reservation_merchant
    ON reservations (merchant_id);