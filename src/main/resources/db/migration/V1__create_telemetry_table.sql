CREATE TABLE telemetry (
                           id BIGSERIAL PRIMARY KEY,
                           vehicle_id VARCHAR(100) NOT NULL,
                           latitude DOUBLE PRECISION NOT NULL,
                           longitude DOUBLE PRECISION NOT NULL,
                           telemetry_timestamp TIMESTAMPTZ NOT NULL,
                           created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                           CONSTRAINT chk_telemetry_latitude
                               CHECK (latitude >= -90 AND latitude <= 90),

                           CONSTRAINT chk_telemetry_longitude
                               CHECK (longitude >= -180 AND longitude <= 180)
);

CREATE INDEX idx_telemetry_vehicle_id
    ON telemetry(vehicle_id);

CREATE INDEX idx_telemetry_vehicle_timestamp
    ON telemetry(vehicle_id, telemetry_timestamp);