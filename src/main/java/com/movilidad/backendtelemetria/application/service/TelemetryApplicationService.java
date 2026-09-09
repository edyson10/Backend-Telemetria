package com.movilidad.backendtelemetria.application.service;

import com.movilidad.backendtelemetria.application.port.input.IngestTelemetryUseCase;
import com.movilidad.backendtelemetria.application.port.output.AlertRepositoryPort;
import com.movilidad.backendtelemetria.application.port.output.TelemetryCachePort;
import com.movilidad.backendtelemetria.application.port.output.TelemetryRepositoryPort;
import com.movilidad.backendtelemetria.domain.model.LatestVehicleTelemetry;
import com.movilidad.backendtelemetria.domain.model.Telemetry;
import com.movilidad.backendtelemetria.domain.model.TelemetryAlert;
import com.movilidad.backendtelemetria.domain.model.VehicleStatus;

import java.util.Optional;

public class TelemetryApplicationService
        implements IngestTelemetryUseCase {

    private final TelemetryRepositoryPort telemetryRepositoryPort;
    private final TelemetryCachePort telemetryCachePort;
    private final VehicleStopDetectionService vehicleStopDetectionService;
    private final AlertRepositoryPort alertRepositoryPort;

    public TelemetryApplicationService(
            TelemetryRepositoryPort telemetryRepositoryPort,
            TelemetryCachePort telemetryCachePort,
            VehicleStopDetectionService vehicleStopDetectionService,
            AlertRepositoryPort alertRepositoryPort
    ) {
        this.telemetryRepositoryPort = telemetryRepositoryPort;
        this.telemetryCachePort = telemetryCachePort;
        this.vehicleStopDetectionService = vehicleStopDetectionService;
        this.alertRepositoryPort = alertRepositoryPort;
    }

    @Override
    public TelemetryIngestionResult ingest(Telemetry telemetry) {

        boolean newTelemetry =
                telemetryCachePort.registerIfNotDuplicate(telemetry);

        if (!newTelemetry) {

            VehicleStatus status =
                    telemetryCachePort.findLatest(
                                    telemetry.getVehicleId()
                            )
                            .map(LatestVehicleTelemetry::status)
                            .orElse(VehicleStatus.MOVING);

            return TelemetryIngestionResult.duplicated(
                    telemetry,
                    status
            );
        }

        Optional<Telemetry> previousTelemetry =
                telemetryCachePort.findLatest(
                                telemetry.getVehicleId()
                        )
                        .map(this::toTelemetry)
                        .or(() ->
                                telemetryRepositoryPort.findLatestByVehicleId(
                                        telemetry.getVehicleId()
                                )
                        );

        VehicleStatus status =
                vehicleStopDetectionService.evaluate(
                        telemetry,
                        previousTelemetry
                );

        if (status == VehicleStatus.STOPPED) {

            TelemetryAlert alert =
                    TelemetryAlert.vehicleStopped(
                            telemetry.getVehicleId(),
                            telemetry.getTimestamp()
                    );

            alertRepositoryPort.save(alert);
        }

        telemetryRepositoryPort.save(telemetry);

        telemetryCachePort.saveLatest(
                telemetry,
                status
        );

        return TelemetryIngestionResult.accepted(
                telemetry,
                status
        );
    }

    private Telemetry toTelemetry(
            LatestVehicleTelemetry latestTelemetry
    ) {
        return Telemetry.create(
                latestTelemetry.vehicleId(),
                latestTelemetry.latitude(),
                latestTelemetry.longitude(),
                latestTelemetry.timestamp()
        );
    }
}