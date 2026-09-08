package com.movilidad.backendtelemetria.infrastructure.adapter.input.rest;

import java.time.Instant;

public record TelemetryResponse(

        String vehicleId,

        double lat,

        double lng,

        Instant timestamp,

        String status
) {
}
