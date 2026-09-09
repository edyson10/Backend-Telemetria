package com.movilidad.backendtelemetria.infrastructure.adapter.input.rest.response;

import java.time.Instant;

public record AlertResponse(
        String vehicleId,
        String type,
        String message,
        Instant timestamp
) {
}
