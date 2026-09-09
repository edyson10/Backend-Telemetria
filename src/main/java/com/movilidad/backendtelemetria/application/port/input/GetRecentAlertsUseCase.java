package com.movilidad.backendtelemetria.application.port.input;

import com.movilidad.backendtelemetria.domain.model.TelemetryAlert;

import java.util.List;

public interface GetRecentAlertsUseCase {
    List<TelemetryAlert> getRecentAlerts(int limit);
}
