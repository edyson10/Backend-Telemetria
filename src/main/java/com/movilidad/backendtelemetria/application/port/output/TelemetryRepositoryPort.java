package com.movilidad.backendtelemetria.application.port.output;

import com.movilidad.backendtelemetria.domain.model.Telemetry;

import java.util.List;
import java.util.Optional;

public interface TelemetryRepositoryPort {
    void save(Telemetry telemetry);
    List<String> findVehicleIds();
    Optional<Telemetry> findLatestByVehicleId(String vehicleId);
}
