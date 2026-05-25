CREATE TABLE rentals (
    id            BIGSERIAL   PRIMARY KEY,
    user_id       BIGINT      NOT NULL,
    scooter_id    BIGINT      NOT NULL REFERENCES scooters(id),
    started_at    TIMESTAMP   NOT NULL,
    ended_at      TIMESTAMP,
    status        VARCHAR(20) NOT NULL,
    total_minutes INT
);
