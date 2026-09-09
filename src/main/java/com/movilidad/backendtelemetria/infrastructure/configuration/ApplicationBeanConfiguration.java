package com.movilidad.backendtelemetria.infrastructure.configuration;

import com.movilidad.backendtelemetria.application.port.input.CalculateRouteUseCase;
import com.movilidad.backendtelemetria.application.port.input.GetRecentAlertsUseCase;
import com.movilidad.backendtelemetria.application.port.input.GetVehiclesUseCase;
import com.movilidad.backendtelemetria.application.port.input.IngestTelemetryUseCase;
import com.movilidad.backendtelemetria.application.port.output.AlertRepositoryPort;
import com.movilidad.backendtelemetria.application.port.output.RouteServicePort;
import com.movilidad.backendtelemetria.application.port.output.TelemetryCachePort;
import com.movilidad.backendtelemetria.application.port.output.TelemetryRepositoryPort;
import com.movilidad.backendtelemetria.application.service.*;
import com.movilidad.backendtelemetria.infrastructure.adapter.output.route.RouteServiceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(RouteServiceProperties.class)
public class ApplicationBeanConfiguration {

    @Bean
    public VehicleStopDetectionService vehicleStopDetectionService(
            TelemetryCachePort telemetryCachePort
    ) {
        return new VehicleStopDetectionService(
                telemetryCachePort
        );
    }

    @Bean
    public IngestTelemetryUseCase ingestTelemetryUseCase(
            TelemetryRepositoryPort telemetryRepositoryPort,
            TelemetryCachePort telemetryCachePort,
            VehicleStopDetectionService vehicleStopDetectionService,
            AlertRepositoryPort alertRepositoryPort
    ) {
        return new TelemetryApplicationService(
                telemetryRepositoryPort,
                telemetryCachePort,
                vehicleStopDetectionService,
                alertRepositoryPort
        );
    }

    @Bean
    public GetRecentAlertsUseCase getRecentAlertsUseCase(
            AlertRepositoryPort alertRepositoryPort
    ) {
        return new AlertApplicationService(
                alertRepositoryPort
        );
    }

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public CalculateRouteUseCase calculateRouteUseCase(
            RouteServicePort routeServicePort
    ) {
        return new RouteCalculationService(
                routeServicePort
        );
    }

    @Bean
    public GetVehiclesUseCase getVehiclesUseCase(
            TelemetryRepositoryPort telemetryRepositoryPort,
            TelemetryCachePort telemetryCachePort
    ) {
        return new VehicleQueryService(
                telemetryRepositoryPort,
                telemetryCachePort
        );
    }
}
