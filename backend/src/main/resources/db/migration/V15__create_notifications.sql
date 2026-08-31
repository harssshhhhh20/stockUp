CREATE TABLE notifications
(
    id UUID PRIMARY KEY,

    recipient_id UUID NOT NULL,

    type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    reference_id UUID,

    read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP WITH TIME ZONE,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

ALTER TABLE notifications
    ADD CONSTRAINT fk_notification_recipient
        FOREIGN KEY (recipient_id)
            REFERENCES users (id);

CREATE INDEX idx_notification_recipient_created_at
    ON notifications (recipient_id, created_at DESC);

CREATE INDEX idx_notification_recipient_unread
    ON notifications (recipient_id, read);
