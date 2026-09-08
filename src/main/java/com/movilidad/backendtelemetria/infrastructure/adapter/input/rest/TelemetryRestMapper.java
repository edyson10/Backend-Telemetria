package com.movilidad.backendtelemetria.infrastructure.adapter.input.rest;

import com.movilidad.backendtelemetria.domain.model.Telemetry;
import org.springframework.stereotype.Component;

@Component
public class TelemetryRestMapper {

    public Telemetry toDomain(TelemetryRequest request) {

        return Telemetry.create(
                request.vehicleId(),
                request.lat(),
                request.lng(),
                request.timestamp()
        );
    }

    public TelemetryResponse toResponse(Telemetry telemetry) {

        return new TelemetryResponse(
                telemetry.getVehicleId(),
                telemetry.getLatitude(),
                telemetry.getLongitude(),
                telemetry.getTimestamp(),
                "INGESTED"
        );
    }
}