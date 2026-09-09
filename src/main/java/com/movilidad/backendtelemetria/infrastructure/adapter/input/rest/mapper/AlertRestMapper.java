package com.movilidad.backendtelemetria.infrastructure.adapter.input.rest.mapper;

import com.movilidad.backendtelemetria.domain.model.TelemetryAlert;
import com.movilidad.backendtelemetria.infrastructure.adapter.input.rest.response.AlertResponse;
import org.springframework.stereotype.Component;

@Component
public class AlertRestMapper {

    public AlertResponse toResponse(
            TelemetryAlert alert
    ) {
        return new AlertResponse(
                alert.getVehicleId(),
                alert.getType(),
                alert.getMessage(),
                alert.getTimestamp()
        );
    }
}
