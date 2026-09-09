package com.movilidad.backendtelemetria.infrastructure.adapter.input.rest.response;


import java.time.Instant;

public record VehicleResponse(
        String vehicleId,
        double lat,
        double lng,
        Instant timestamp,
        String status
) {
}
