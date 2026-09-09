package com.movilidad.backendtelemetria.domain.model;

import java.time.Instant;
import java.util.Objects;

public class TelemetryAlert {

    private final String vehicleId;
    private final String type;
    private final String message;
    private final Instant timestamp;

    private TelemetryAlert(
            String vehicleId,
            String type,
            String message,
            Instant timestamp
    ) {
        this.vehicleId = vehicleId;
        this.type = type;
        this.message = message;
        this.timestamp = timestamp;
    }

    public static TelemetryAlert vehicleStopped(
            String vehicleId,
            Instant timestamp
    ) {
        return new TelemetryAlert(
                vehicleId,
                "VEHICLE_STOPPED",
                "Vehículo Detenido",
                timestamp
        );
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object object) {

        if (this == object) {
            return true;
        }

        if (!(object instanceof TelemetryAlert other)) {
            return false;
        }

        return Objects.equals(vehicleId, other.vehicleId)
                && Objects.equals(type, other.type)
                && Objects.equals(timestamp, other.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                vehicleId,
                type,
                timestamp
        );
    }
}