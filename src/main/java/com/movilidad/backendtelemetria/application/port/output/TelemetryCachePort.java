package com.movilidad.backendtelemetria.application.port.output;

import com.movilidad.backendtelemetria.domain.model.Telemetry;

public interface TelemetryCachePort {
    boolean registerIfNotDuplicate(Telemetry telemetry);
    void saveLatest(Telemetry telemetry);
}
