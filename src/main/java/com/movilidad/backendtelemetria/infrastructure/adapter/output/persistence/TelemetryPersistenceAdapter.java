package com.movilidad.backendtelemetria.infrastructure.adapter.output.persistence;

import com.movilidad.backendtelemetria.application.port.output.TelemetryRepositoryPort;
import com.movilidad.backendtelemetria.domain.model.Telemetry;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Repository
public class TelemetryPersistenceAdapter
        implements TelemetryRepositoryPort {

    private final TelemetryJpaRepository repository;
    private final TelemetryPersistenceMapper mapper;

    public TelemetryPersistenceAdapter(
            TelemetryJpaRepository repository,
            TelemetryPersistenceMapper mapper) {

        this.repository = Objects.requireNonNull(
                repository,
                "Telemetry JPA repository cannot be null"
        );

        this.mapper = Objects.requireNonNull(
                mapper,
                "Telemetry persistence mapper cannot be null"
        );
    }

    @Override
    @Transactional
    public void save(Telemetry telemetry) {

        Objects.requireNonNull(
                telemetry,
                "Telemetry cannot be null"
        );

        TelemetryEntity entity =
                mapper.toEntity(telemetry);

        repository.save(entity);
    }
}
