CREATE TABLE users (
    id            BIGSERIAL    PRIMARY KEY,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(200) NOT NULL,
    role          VARCHAR(20)  NOT NULL CHECK (role IN ('CLIENT', 'ADMIN')),
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    active        BOOLEAN      NOT NULL DEFAULT TRUE
);
