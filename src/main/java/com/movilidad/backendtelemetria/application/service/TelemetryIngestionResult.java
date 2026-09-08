package com.movilidad.backendtelemetria.application.service;

import com.movilidad.backendtelemetria.domain.model.Telemetry;

public record TelemetryIngestionResult(
        Telemetry telemetry,
        boolean duplicate
) {

    public static TelemetryIngestionResult accepted(Telemetry telemetry) {
        return new TelemetryIngestionResult(telemetry, false);
    }

    public static TelemetryIngestionResult duplicated(Telemetry telemetry) {
        return new TelemetryIngestionResult(telemetry, true);
    }
}
