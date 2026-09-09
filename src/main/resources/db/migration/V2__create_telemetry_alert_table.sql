CREATE TABLE telemetry_alert (
                                 id BIGSERIAL PRIMARY KEY,
                                 vehicle_id VARCHAR(100) NOT NULL,
                                 alert_type VARCHAR(100) NOT NULL,
                                 message VARCHAR(255) NOT NULL,
                                 alert_timestamp TIMESTAMPTZ NOT NULL,
                                 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_telemetry_alert_vehicle_id
    ON telemetry_alert(vehicle_id);

CREATE INDEX idx_telemetry_alert_timestamp
    ON telemetry_alert(alert_timestamp);