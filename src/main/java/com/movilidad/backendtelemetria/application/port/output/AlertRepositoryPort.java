package com.movilidad.backendtelemetria.application.port.output;

import com.movilidad.backendtelemetria.domain.model.TelemetryAlert;

import java.util.List;

public interface AlertRepositoryPort {
    TelemetryAlert save(TelemetryAlert alert);
    List<TelemetryAlert> findRecent(int limit);
}
