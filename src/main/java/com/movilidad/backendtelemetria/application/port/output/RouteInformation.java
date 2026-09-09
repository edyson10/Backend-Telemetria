package com.movilidad.backendtelemetria.application.port.output;

public record RouteInformation(
        double distanceKm,
        int estimatedMinutes
) {
}