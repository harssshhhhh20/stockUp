-- Verified feedback: a rating exists only because an order existed.
--
-- The UNIQUE constraint on reservation_id is the anti-spam mechanism. There is
-- no path to submit a review that isn't attached to a real, completed order,
-- and no way to submit twice.

CREATE TABLE reservation_feedback
(
    id UUID PRIMARY KEY,

    reservation_id UUID NOT NULL UNIQUE,
    customer_id UUID NOT NULL,
    merchant_id UUID NOT NULL,
    store_id UUID NOT NULL,

    stars SMALLINT NOT NULL CHECK (stars BETWEEN 1 AND 5),

    -- Structured chips. Each is a second opinion on one Bharosa pillar, which
    -- is what lets feedback corroborate behaviour rather than merely decorate it.
    -- Null means the customer didn't answer that one.
    replied_fast BOOLEAN,
    ready_on_time BOOLEAN,
    stock_accurate BOOLEAN,

    comment VARCHAR(1000),

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

ALTER TABLE reservation_feedback
    ADD CONSTRAINT fk_feedback_reservation FOREIGN KEY (reservation_id) REFERENCES reservations (id);
ALTER TABLE reservation_feedback
    ADD CONSTRAINT fk_feedback_customer FOREIGN KEY (customer_id) REFERENCES users (id);
ALTER TABLE reservation_feedback
    ADD CONSTRAINT fk_feedback_merchant FOREIGN KEY (merchant_id) REFERENCES merchants (id);
ALTER TABLE reservation_feedback
    ADD CONSTRAINT fk_feedback_store FOREIGN KEY (store_id) REFERENCES stores (id);

CREATE INDEX idx_feedback_merchant_time ON reservation_feedback (merchant_id, created_at DESC);
CREATE INDEX idx_feedback_store_time ON reservation_feedback (store_id, created_at DESC);
