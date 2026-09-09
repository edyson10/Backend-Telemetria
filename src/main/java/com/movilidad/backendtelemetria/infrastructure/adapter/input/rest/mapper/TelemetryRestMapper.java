package com.movilidad.backendtelemetria.infrastructure.adapter.input.rest.mapper;

import com.movilidad.backendtelemetria.domain.model.Telemetry;
import com.movilidad.backendtelemetria.domain.model.VehicleStatus;
import com.movilidad.backendtelemetria.infrastructure.adapter.input.rest.request.TelemetryRequest;
import com.movilidad.backendtelemetria.infrastructure.adapter.input.rest.response.TelemetryResponse;
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

    public TelemetryResponse toResponse(
            Telemetry telemetry,
            VehicleStatus status
    ) {

        return new TelemetryResponse(
                telemetry.getVehicleId(),
                telemetry.getLatitude(),
                telemetry.getLongitude(),
                telemetry.getTimestamp(),
                status.name()
        );
    }
}