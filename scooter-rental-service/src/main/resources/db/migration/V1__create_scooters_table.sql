CREATE TABLE scooters (
    id          BIGSERIAL    PRIMARY KEY,
    serial_code VARCHAR(50)  NOT NULL UNIQUE,
    model       VARCHAR(80)  NOT NULL,
    battery     INT          NOT NULL CHECK (battery >= 0 AND battery <= 100),
    location    VARCHAR(100) NOT NULL,
    status      VARCHAR(20)  NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);
