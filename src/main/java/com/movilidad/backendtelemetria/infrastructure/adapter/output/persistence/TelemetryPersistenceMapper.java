package com.movilidad.backendtelemetria.infrastructure.adapter.output.persistence;

import com.movilidad.backendtelemetria.domain.model.Telemetry;
import org.springframework.stereotype.Component;

@Component
public class TelemetryPersistenceMapper {

    public TelemetryEntity toEntity(Telemetry telemetry) {

        return new TelemetryEntity(
                telemetry.getVehicleId(),
                telemetry.getLatitude(),
                telemetry.getLongitude(),
                telemetry.getTimestamp()
        );
    }
}
