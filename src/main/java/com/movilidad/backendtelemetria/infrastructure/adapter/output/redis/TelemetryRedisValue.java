package com.movilidad.backendtelemetria.infrastructure.adapter.output.redis;

import com.movilidad.backendtelemetria.domain.model.VehicleStatus;

import java.time.Instant;

public record TelemetryRedisValue(
        String vehicleId,
        double latitude,
        double longitude,
        Instant timestamp,
        VehicleStatus status
) {
}
