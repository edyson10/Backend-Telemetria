package com.movilidad.backendtelemetria.infrastructure.adapter.output.persistence.mapper;

import com.movilidad.backendtelemetria.domain.model.TelemetryAlert;
import com.movilidad.backendtelemetria.infrastructure.adapter.output.persistence.entity.TelemetryAlertEntity;
import org.springframework.stereotype.Component;

@Component
public class TelemetryAlertPersistenceMapper {

    public TelemetryAlertEntity toEntity(
            TelemetryAlert alert
    ) {
        return new TelemetryAlertEntity(
                alert.getVehicleId(),
                alert.getType(),
                alert.getMessage(),
                alert.getTimestamp()
        );
    }

    public TelemetryAlert toDomain(
            TelemetryAlertEntity entity
    ) {
        return TelemetryAlert.vehicleStopped(
                entity.getVehicleId(),
                entity.getAlertTimestamp()
        );
    }
}