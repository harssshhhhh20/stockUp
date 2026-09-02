-- The append-only record of everything that happens to a request.
--
-- Order timelines, merchant statistics and every Bharosa signal are all read
-- from this one table, so a number shown to a merchant can never disagree with
-- the number that scored them.
--
-- Rows are never updated or deleted.

CREATE TABLE reservation_events
(
    id UUID PRIMARY KEY,

    -- Null until a request is actually reserved: the earlier events in the
    -- funnel (broadcast, view, offer) happen before a reservation exists.
    reservation_id UUID,

    -- Always present — every event belongs to a request from a customer.
    basket_id UUID NOT NULL,

    -- Which shop this event is about. Null only for basket-wide events.
    store_id UUID,
    merchant_id UUID,

    event_type VARCHAR(50) NOT NULL,

    -- Who caused it: CUSTOMER, MERCHANT or SYSTEM (schedulers).
    actor VARCHAR(20) NOT NULL,

    -- When it actually happened, which is not always when it was recorded
    -- (backfilled rows carry their original timestamp).
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,

    -- Small typed payload: latency in millis, cancellation reason, etc.
    metadata JSONB,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

ALTER TABLE reservation_events
    ADD CONSTRAINT fk_revent_basket FOREIGN KEY (basket_id) REFERENCES baskets (id);

ALTER TABLE reservation_events
    ADD CONSTRAINT fk_revent_reservation FOREIGN KEY (reservation_id) REFERENCES reservations (id);

ALTER TABLE reservation_events
    ADD CONSTRAINT fk_revent_store FOREIGN KEY (store_id) REFERENCES stores (id);

ALTER TABLE reservation_events
    ADD CONSTRAINT fk_revent_merchant FOREIGN KEY (merchant_id) REFERENCES merchants (id);

-- Scoring reads "everything this merchant did recently", so this is the hot path.
CREATE INDEX idx_revent_merchant_time ON reservation_events (merchant_id, occurred_at DESC);
CREATE INDEX idx_revent_basket ON reservation_events (basket_id, occurred_at);
CREATE INDEX idx_revent_reservation ON reservation_events (reservation_id, occurred_at);
CREATE INDEX idx_revent_type_time ON reservation_events (event_type, occurred_at DESC);


-- ---------------------------------------------------------------------------
-- Backfill from the timestamps already scattered across the existing tables,
-- so history is preserved and Bharosa has something to score from day one.
-- ---------------------------------------------------------------------------

-- A request reached a shop.
INSERT INTO reservation_events
    (id, basket_id, store_id, merchant_id, event_type, actor, occurred_at, created_at, updated_at)
SELECT gen_random_uuid(), b.basket_id, br.store_id, s.merchant_id,
       'REQUEST_BROADCAST', 'SYSTEM', br.created_at, now(), now()
FROM broadcast_recipients br
JOIN broadcasts b ON b.id = br.broadcast_id
JOIN stores s ON s.id = br.store_id;

-- The shop opened it.
INSERT INTO reservation_events
    (id, basket_id, store_id, merchant_id, event_type, actor, occurred_at, created_at, updated_at)
SELECT gen_random_uuid(), b.basket_id, br.store_id, s.merchant_id,
       'MERCHANT_VIEWED', 'MERCHANT', br.viewed_at, now(), now()
FROM broadcast_recipients br
JOIN broadcasts b ON b.id = br.broadcast_id
JOIN stores s ON s.id = br.store_id
WHERE br.viewed_at IS NOT NULL;

-- The shop answered.
INSERT INTO reservation_events
    (id, basket_id, store_id, merchant_id, event_type, actor, occurred_at, created_at, updated_at)
SELECT gen_random_uuid(), b.basket_id, br.store_id, s.merchant_id,
       'OFFER_SUBMITTED', 'MERCHANT', br.responded_at, now(), now()
FROM broadcast_recipients br
JOIN broadcasts b ON b.id = br.broadcast_id
JOIN stores s ON s.id = br.store_id
WHERE br.responded_at IS NOT NULL;

-- The shop let it lapse. Seen-then-ignored is recorded distinctly from
-- never-opened, because Bharosa weighs them very differently.
INSERT INTO reservation_events
    (id, basket_id, store_id, merchant_id, event_type, actor, occurred_at, created_at, updated_at)
SELECT gen_random_uuid(), b.basket_id, br.store_id, s.merchant_id,
       CASE WHEN br.viewed_at IS NOT NULL
            THEN 'REQUEST_VIEWED_THEN_EXPIRED'
            ELSE 'REQUEST_EXPIRED_UNSEEN' END,
       'SYSTEM', bk.expires_at, now(), now()
FROM broadcast_recipients br
JOIN broadcasts b ON b.id = br.broadcast_id
JOIN baskets bk ON bk.id = b.basket_id
JOIN stores s ON s.id = br.store_id
WHERE br.status = 'EXPIRED';

-- The customer committed.
INSERT INTO reservation_events
    (id, reservation_id, basket_id, store_id, merchant_id, event_type, actor, occurred_at, created_at, updated_at)
SELECT gen_random_uuid(), r.id, r.basket_id, r.store_id, r.merchant_id,
       'CUSTOMER_RESERVED', 'CUSTOMER', r.created_at, now(), now()
FROM reservations r;

-- The hold went live and an OTP was issued.
INSERT INTO reservation_events
    (id, reservation_id, basket_id, store_id, merchant_id, event_type, actor, occurred_at, created_at, updated_at)
SELECT gen_random_uuid(), r.id, r.basket_id, r.store_id, r.merchant_id,
       'RESERVATION_ACTIVATED', 'SYSTEM', r.active_at, now(), now()
FROM reservations r
WHERE r.active_at IS NOT NULL;

-- How it ended.
INSERT INTO reservation_events
    (id, reservation_id, basket_id, store_id, merchant_id, event_type, actor, occurred_at, created_at, updated_at)
SELECT gen_random_uuid(), r.id, r.basket_id, r.store_id, r.merchant_id,
       CASE r.status
           WHEN 'COMPLETED'          THEN 'HANDOVER_COMPLETED'
           WHEN 'MERCHANT_CANCELLED' THEN 'MERCHANT_CANCELLED'
           WHEN 'CUSTOMER_CANCELLED' THEN 'CUSTOMER_CANCELLED'
           WHEN 'EXPIRED'            THEN 'RESERVATION_EXPIRED'
       END,
       CASE r.status
           WHEN 'MERCHANT_CANCELLED' THEN 'MERCHANT'
           WHEN 'CUSTOMER_CANCELLED' THEN 'CUSTOMER'
           ELSE 'SYSTEM'
       END,
       r.updated_at, now(), now()
FROM reservations r
WHERE r.status IN ('COMPLETED', 'MERCHANT_CANCELLED', 'CUSTOMER_CANCELLED', 'EXPIRED');
