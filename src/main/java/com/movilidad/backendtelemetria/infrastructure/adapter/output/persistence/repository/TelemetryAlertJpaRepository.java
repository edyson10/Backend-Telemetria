package com.movilidad.backendtelemetria.infrastructure.adapter.output.persistence.repository;

import com.movilidad.backendtelemetria.infrastructure.adapter.output.persistence.entity.TelemetryAlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TelemetryAlertJpaRepository
        extends JpaRepository<TelemetryAlertEntity, Long> {

    List<TelemetryAlertEntity>
    findTop100ByOrderByAlertTimestampDesc();
}
