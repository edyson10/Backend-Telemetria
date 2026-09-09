package com.movilidad.backendtelemetria.infrastructure.adapter.output.persistence.mapper;

import com.movilidad.backendtelemetria.domain.model.Telemetry;
import com.movilidad.backendtelemetria.infrastructure.adapter.output.persistence.entity.TelemetryEntity;
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

    public Telemetry toDomain(TelemetryEntity entity) {

        return Telemetry.create(
                entity.getVehicleId(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getTimestamp()
        );
    }
}
