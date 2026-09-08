package com.movilidad.backendtelemetria.application.port.output;

import com.movilidad.backendtelemetria.domain.model.Telemetry;

public interface TelemetryRepositoryPort {
    void save(Telemetry telemetry);
}
