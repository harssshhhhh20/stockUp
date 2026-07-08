-- Initial database schema

CREATE TABLE IF NOT EXISTS flyway_bootstrap
(
    id INT PRIMARY KEY
);

INSERT INTO flyway_bootstrap(id)
VALUES (1)
ON CONFLICT DO NOTHING;