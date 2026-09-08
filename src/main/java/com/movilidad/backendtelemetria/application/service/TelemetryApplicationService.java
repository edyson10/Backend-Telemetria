package com.movilidad.backendtelemetria.application.service;

import com.movilidad.backendtelemetria.application.port.input.IngestTelemetryUseCase;
import com.movilidad.backendtelemetria.application.port.output.TelemetryCachePort;
import com.movilidad.backendtelemetria.application.port.output.TelemetryRepositoryPort;
import com.movilidad.backendtelemetria.domain.model.Telemetry;

public class TelemetryApplicationService
        implements IngestTelemetryUseCase {

    private final TelemetryRepositoryPort telemetryRepositoryPort;
    private final TelemetryCachePort telemetryCachePort;

    public TelemetryApplicationService(
            TelemetryRepositoryPort telemetryRepositoryPort,
            TelemetryCachePort telemetryCachePort
    ) {
        this.telemetryRepositoryPort = telemetryRepositoryPort;
        this.telemetryCachePort = telemetryCachePort;
    }

    @Override
    public TelemetryIngestionResult ingest(Telemetry telemetry) {

        boolean newTelemetry =
                telemetryCachePort.registerIfNotDuplicate(telemetry);

        if (!newTelemetry) {
            return TelemetryIngestionResult.duplicated(telemetry);
        }

        telemetryRepositoryPort.save(telemetry);

        telemetryCachePort.saveLatest(telemetry);

        return TelemetryIngestionResult.accepted(telemetry);
    }
}
