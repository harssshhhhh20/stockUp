CREATE TABLE baskets
(
    id                     UUID         NOT NULL PRIMARY KEY,

    customer_id            UUID         NOT NULL,

    target_mode            VARCHAR(30)  NOT NULL,

    status                 VARCHAR(30)  NOT NULL,

    search_radius   INTEGER,

    basket_latitude      NUMERIC(10, 7) NOT NULL,

    basket_longitude     NUMERIC(10, 7) NOT NULL,

    expires_at             TIMESTAMP    NOT NULL,

    created_at             TIMESTAMP    NOT NULL,

    updated_at             TIMESTAMP    NOT NULL,

    CONSTRAINT fk_baskets_customer
        FOREIGN KEY (customer_id)
            REFERENCES users (id)
);

CREATE TABLE basket_items
(
    id              UUID            NOT NULL PRIMARY KEY,

    basket_id       UUID            NOT NULL,

    product_name    VARCHAR(255)    NOT NULL,

    quantity        NUMERIC(10, 2)  NOT NULL,

    unit            VARCHAR(30)     NOT NULL,

    brand           VARCHAR(100),

    notes           VARCHAR(500),

    created_at      TIMESTAMP       NOT NULL,

    updated_at      TIMESTAMP       NOT NULL,

    CONSTRAINT fk_basket_items_basket
        FOREIGN KEY (basket_id)
            REFERENCES baskets (id)
            ON DELETE CASCADE
);

CREATE TABLE basket_target_stores
(
    id              UUID         NOT NULL PRIMARY KEY,

    basket_id       UUID         NOT NULL,

    store_id        UUID         NOT NULL,

    created_at      TIMESTAMP    NOT NULL,

    updated_at      TIMESTAMP    NOT NULL,

    CONSTRAINT fk_basket_target_stores_basket
        FOREIGN KEY (basket_id)
            REFERENCES baskets (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_basket_target_stores_store
        FOREIGN KEY (store_id)
            REFERENCES stores (id),

    CONSTRAINT uk_basket_target_store
        UNIQUE (basket_id, store_id)
);

CREATE INDEX idx_baskets_customer
    ON baskets (customer_id);

CREATE INDEX idx_basket_items_basket
    ON basket_items (basket_id);

CREATE INDEX idx_basket_target_stores_basket
    ON basket_target_stores (basket_id);

CREATE INDEX idx_basket_target_stores_store
    ON basket_target_stores (store_id);