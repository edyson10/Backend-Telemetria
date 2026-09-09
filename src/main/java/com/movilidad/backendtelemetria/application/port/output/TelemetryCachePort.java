package com.movilidad.backendtelemetria.application.port.output;

import com.movilidad.backendtelemetria.domain.model.LatestVehicleTelemetry;
import com.movilidad.backendtelemetria.domain.model.Telemetry;
import com.movilidad.backendtelemetria.domain.model.VehicleStatus;

import java.time.Instant;
import java.util.Optional;

public interface TelemetryCachePort {
    boolean registerIfNotDuplicate(Telemetry telemetry);
    void saveLatest(
            Telemetry telemetry,
            VehicleStatus status
    );

    Optional<LatestVehicleTelemetry> findLatest(String vehicleId);

    Optional<Instant> findStopStart(String vehicleId);

    void saveStopStart(
            String vehicleId,
            Instant timestamp
    );

    void deleteStopStart(String vehicleId);

}
