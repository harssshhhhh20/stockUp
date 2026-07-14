CREATE TABLE broadcasts
(
    id UUID PRIMARY KEY,

    basket_id UUID NOT NULL UNIQUE,

    status VARCHAR(50) NOT NULL,

    completed_at TIMESTAMP,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_broadcast_basket
        FOREIGN KEY (basket_id)
            REFERENCES baskets (id)
            ON DELETE CASCADE
);

CREATE TABLE broadcast_recipients
(
    id UUID PRIMARY KEY,

    broadcast_id UUID NOT NULL,

    store_id UUID NOT NULL,

    status VARCHAR(50) NOT NULL,

    viewed_at TIMESTAMP,

    responded_at TIMESTAMP,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_recipient_broadcast
        FOREIGN KEY (broadcast_id)
            REFERENCES broadcasts (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_recipient_store
        FOREIGN KEY (store_id)
            REFERENCES stores (id)
);