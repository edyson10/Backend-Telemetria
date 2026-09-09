package com.movilidad.backendtelemetria.application.port.output;

import com.movilidad.backendtelemetria.domain.model.Telemetry;

import java.util.Optional;

public interface VehiclePositionCachePort {
    Optional<Telemetry> findLatest(String vehicleId);
}
