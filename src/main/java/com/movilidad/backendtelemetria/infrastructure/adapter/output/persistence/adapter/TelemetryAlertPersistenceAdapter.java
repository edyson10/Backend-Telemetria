package com.movilidad.backendtelemetria.infrastructure.adapter.output.persistence.adapter;

import com.movilidad.backendtelemetria.application.port.output.AlertRepositoryPort;
import com.movilidad.backendtelemetria.domain.model.TelemetryAlert;
import com.movilidad.backendtelemetria.infrastructure.adapter.output.persistence.mapper.TelemetryAlertPersistenceMapper;
import com.movilidad.backendtelemetria.infrastructure.adapter.output.persistence.entity.TelemetryAlertEntity;
import com.movilidad.backendtelemetria.infrastructure.adapter.output.persistence.repository.TelemetryAlertJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TelemetryAlertPersistenceAdapter
        implements AlertRepositoryPort {

    private final TelemetryAlertJpaRepository repository;
    private final TelemetryAlertPersistenceMapper mapper;

    public TelemetryAlertPersistenceAdapter(
            TelemetryAlertJpaRepository repository,
            TelemetryAlertPersistenceMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public TelemetryAlert save(TelemetryAlert alert) {

        TelemetryAlertEntity entity =
                mapper.toEntity(alert);

        TelemetryAlertEntity saved =
                repository.save(entity);

        return mapper.toDomain(saved);
    }

    @Override
    public List<TelemetryAlert> findRecent(int limit) {

        return repository
                .findTop100ByOrderByAlertTimestampDesc()
                .stream()
                .limit(limit)
                .map(mapper::toDomain)
                .toList();
    }
}
