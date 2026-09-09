package com.movilidad.backendtelemetria.application.service;

import com.movilidad.backendtelemetria.application.port.input.GetRecentAlertsUseCase;
import com.movilidad.backendtelemetria.application.port.output.AlertRepositoryPort;
import com.movilidad.backendtelemetria.domain.model.TelemetryAlert;

import java.util.List;

public class AlertApplicationService
        implements GetRecentAlertsUseCase {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final AlertRepositoryPort alertRepositoryPort;

    public AlertApplicationService(
            AlertRepositoryPort alertRepositoryPort
    ) {
        this.alertRepositoryPort = alertRepositoryPort;
    }

    @Override
    public List<TelemetryAlert> getRecentAlerts(int limit) {

        int normalizedLimit = limit <= 0
                ? DEFAULT_LIMIT
                : Math.min(limit, MAX_LIMIT);

        return alertRepositoryPort.findRecent(
                normalizedLimit
        );
    }
}
