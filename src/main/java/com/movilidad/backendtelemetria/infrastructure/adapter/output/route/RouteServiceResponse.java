package com.movilidad.backendtelemetria.infrastructure.adapter.output.route;

public record RouteServiceResponse(
        double distanceKm,
        int estimatedMinutes
) {
}