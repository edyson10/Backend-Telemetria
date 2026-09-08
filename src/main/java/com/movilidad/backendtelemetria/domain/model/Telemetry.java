package com.movilidad.backendtelemetria.domain.model;

import com.movilidad.backendtelemetria.domain.exception.InvalidTelemetryException;

import java.time.Instant;
import java.util.Objects;

public final class Telemetry {

    private final String vehicleId;
    private final double latitude;
    private final double longitude;
    private final Instant timestamp;

    private Telemetry(
            String vehicleId,
            double latitude,
            double longitude,
            Instant timestamp) {

        this.vehicleId = vehicleId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    public static Telemetry create(
            String vehicleId,
            double latitude,
            double longitude,
            Instant timestamp) {

        validateVehicleId(vehicleId);
        validateCoordinates(latitude, longitude);
        validateTimestamp(timestamp);

        return new Telemetry(
                vehicleId,
                latitude,
                longitude,
                timestamp
        );
    }

    private static void validateVehicleId(String vehicleId) {

        if (vehicleId == null || vehicleId.isBlank()) {
            throw new InvalidTelemetryException(
                    "Vehicle ID cannot be null or blank"
            );
        }
    }

    private static void validateCoordinates(
            double latitude,
            double longitude) {

        if (latitude < -90 || latitude > 90) {
            throw new InvalidTelemetryException(
                    "Latitude must be between -90 and 90"
            );
        }

        if (longitude < -180 || longitude > 180) {
            throw new InvalidTelemetryException(
                    "Longitude must be between -180 and 180"
            );
        }
    }

    private static void validateTimestamp(Instant timestamp) {

        if (timestamp == null) {
            throw new InvalidTelemetryException(
                    "Timestamp cannot be null"
            );
        }

        if (timestamp.isAfter(Instant.now())) {
            throw new InvalidTelemetryException(
                    "Timestamp cannot be in the future"
            );
        }
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof Telemetry telemetry)) {
            return false;
        }

        return Double.compare(latitude, telemetry.latitude) == 0
                && Double.compare(longitude, telemetry.longitude) == 0
                && Objects.equals(vehicleId, telemetry.vehicleId)
                && Objects.equals(timestamp, telemetry.timestamp);
    }

    @Override
    public int hashCode() {

        return Objects.hash(
                vehicleId,
                latitude,
                longitude,
                timestamp
        );
    }
}
