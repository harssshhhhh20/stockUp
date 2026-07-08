CREATE TABLE users
(
    id UUID PRIMARY KEY,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    first_name VARCHAR(100),
    last_name VARCHAR(100),

    phone_number VARCHAR(15) NOT NULL UNIQUE,
    email VARCHAR(255) UNIQUE,

    password_hash VARCHAR(255),

    account_status VARCHAR(50) NOT NULL
);

CREATE TABLE user_roles
(
    user_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,

    PRIMARY KEY (user_id, role),

    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);