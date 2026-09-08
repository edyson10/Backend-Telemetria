package com.movilidad.backendtelemetria.infrastructure.adapter.output.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TelemetryJpaRepository
        extends JpaRepository<TelemetryEntity, Long> {
}
