CREATE TABLE merchants
(
    id UUID PRIMARY KEY,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    user_id UUID NOT NULL UNIQUE,

    CONSTRAINT fk_merchants_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);