package com.movilidad.backendtelemetria.application.service;

import com.movilidad.backendtelemetria.application.port.input.GetVehiclesUseCase;
import com.movilidad.backendtelemetria.application.port.output.TelemetryCachePort;
import com.movilidad.backendtelemetria.application.port.output.TelemetryRepositoryPort;
import com.movilidad.backendtelemetria.domain.model.LatestVehicleTelemetry;
import com.movilidad.backendtelemetria.domain.model.Telemetry;
import com.movilidad.backendtelemetria.domain.model.VehicleSnapshot;
import com.movilidad.backendtelemetria.domain.model.VehicleStatus;

import java.util.List;

public class VehicleQueryService implements GetVehiclesUseCase {

    private final TelemetryRepositoryPort telemetryRepositoryPort;
    private final TelemetryCachePort telemetryCachePort;

    public VehicleQueryService(
            TelemetryRepositoryPort telemetryRepositoryPort,
            TelemetryCachePort telemetryCachePort
    ) {
        this.telemetryRepositoryPort = telemetryRepositoryPort;
        this.telemetryCachePort = telemetryCachePort;
    }

    @Override
    public List<VehicleSnapshot> getVehicles() {

        return telemetryRepositoryPort.findVehicleIds()
                .stream()
                .map(this::buildSnapshot)
                .toList();
    }

    private VehicleSnapshot buildSnapshot(String vehicleId) {

        LatestVehicleTelemetry latestTelemetry =
                telemetryCachePort.findLatest(vehicleId)
                        .orElseGet(() ->
                                telemetryRepositoryPort
                                        .findLatestByVehicleId(vehicleId)
                                        .map(this::toLatestVehicleTelemetry)
                                        .orElseThrow(() ->
                                                new IllegalStateException(
                                                        "Telemetry not found for vehicle "
                                                                + vehicleId
                                                )
                                        )
                        );

        return new VehicleSnapshot(
                latestTelemetry.vehicleId(),
                latestTelemetry.latitude(),
                latestTelemetry.longitude(),
                latestTelemetry.timestamp(),
                latestTelemetry.status()
        );
    }

    private LatestVehicleTelemetry toLatestVehicleTelemetry(
            Telemetry telemetry
    ) {
        return new LatestVehicleTelemetry(
                telemetry.getVehicleId(),
                telemetry.getLatitude(),
                telemetry.getLongitude(),
                telemetry.getTimestamp(),
                VehicleStatus.MOVING
        );
    }
}