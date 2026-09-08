package com.movilidad.backendtelemetria.application.port.input;

import com.movilidad.backendtelemetria.application.service.TelemetryIngestionResult;
import com.movilidad.backendtelemetria.domain.model.Telemetry;

public interface IngestTelemetryUseCase {

    TelemetryIngestionResult ingest(Telemetry telemetry);
}
