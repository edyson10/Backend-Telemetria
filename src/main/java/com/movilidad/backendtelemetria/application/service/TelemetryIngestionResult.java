package com.movilidad.backendtelemetria.application.service;

import com.movilidad.backendtelemetria.domain.model.Telemetry;
import com.movilidad.backendtelemetria.domain.model.VehicleStatus;

public record TelemetryIngestionResult(
        Telemetry telemetry,
        boolean duplicate,
        VehicleStatus status
) {

    public static TelemetryIngestionResult accepted(
            Telemetry telemetry,
            VehicleStatus status
    ) {
        return new TelemetryIngestionResult(
                telemetry,
                false,
                status
        );
    }

    public static TelemetryIngestionResult duplicated(
            Telemetry telemetry,
            VehicleStatus status
    ) {
        return new TelemetryIngestionResult(
                telemetry,
                true,
                status
        );
    }
}
