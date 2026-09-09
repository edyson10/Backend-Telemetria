package com.movilidad.backendtelemetria.infrastructure.adapter.output.persistence.repository;

import com.movilidad.backendtelemetria.infrastructure.adapter.output.persistence.entity.TelemetryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TelemetryJpaRepository
        extends JpaRepository<TelemetryEntity, Long> {

    List<TelemetryEntity>
    findTop100ByVehicleIdOrderByTimestampDesc(
            String vehicleId
    );

    List<TelemetryEntity>
    findTop100ByOrderByTimestampDesc();

    @Query("SELECT DISTINCT t.vehicleId FROM TelemetryEntity t")
    List<String> findDistinctVehicleIds();

    Optional<TelemetryEntity>
    findTopByVehicleIdOrderByTimestampDesc(String vehicleId);
}
