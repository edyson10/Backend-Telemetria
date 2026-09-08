package com.movilidad.backendtelemetria.infrastructure.configuration;

import com.movilidad.backendtelemetria.application.port.input.IngestTelemetryUseCase;
import com.movilidad.backendtelemetria.application.port.output.TelemetryCachePort;
import com.movilidad.backendtelemetria.application.port.output.TelemetryRepositoryPort;
import com.movilidad.backendtelemetria.application.service.TelemetryApplicationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationBeanConfiguration {

    @Bean
    public IngestTelemetryUseCase ingestTelemetryUseCase(
            TelemetryRepositoryPort telemetryRepositoryPort,
            TelemetryCachePort telemetryCachePort
    ) {
        return new TelemetryApplicationService(
                telemetryRepositoryPort,
                telemetryCachePort
        );
    }
}
