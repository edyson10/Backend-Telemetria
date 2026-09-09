package com.movilidad.backendtelemetria.domain.model;

import java.time.Instant;

public record LatestVehicleTelemetry(
        String vehicleId,
        double latitude,
        double longitude,
        Instant timestamp,
        VehicleStatus status
) {
}