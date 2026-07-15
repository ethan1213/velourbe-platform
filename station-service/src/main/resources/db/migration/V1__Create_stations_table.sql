CREATE TABLE stations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    address VARCHAR(250) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    capacity INTEGER NOT NULL,
    occupied INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_stations_status  ON stations(status);
CREATE INDEX idx_stations_coords  ON stations(latitude, longitude);
