package com.movilidad.backendtelemetria.application.service;

import com.movilidad.backendtelemetria.application.port.output.TelemetryCachePort;
import com.movilidad.backendtelemetria.application.port.output.TelemetryRepositoryPort;
import com.movilidad.backendtelemetria.domain.model.Telemetry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TelemetryApplicationServiceTest {

    @Mock
    private TelemetryRepositoryPort telemetryRepositoryPort;

    @Mock
    private TelemetryCachePort telemetryCachePort;

    private TelemetryApplicationService service;

    private Telemetry telemetry;

    @BeforeEach
    void setUp() {

        service = new TelemetryApplicationService(
                telemetryRepositoryPort,
                telemetryCachePort
        );

        telemetry = Telemetry.create(
                "VH-001",
                6.2442,
                -75.5812,
                Instant.now()
        );
    }

    @Test
    void shouldPersistTelemetryWhenItIsNotDuplicate() {

        when(telemetryCachePort.registerIfNotDuplicate(telemetry))
                .thenReturn(true);

        TelemetryIngestionResult result =
                service.ingest(telemetry);

        assertFalse(result.duplicate());

        verify(telemetryCachePort)
                .registerIfNotDuplicate(telemetry);

        verify(telemetryRepositoryPort)
                .save(telemetry);

        verify(telemetryCachePort)
                .saveLatest(telemetry);
    }

    @Test
    void shouldIgnoreDuplicateTelemetry() {

        when(telemetryCachePort.registerIfNotDuplicate(telemetry))
                .thenReturn(false);

        TelemetryIngestionResult result =
                service.ingest(telemetry);

        assertTrue(result.duplicate());

        verify(telemetryCachePort)
                .registerIfNotDuplicate(telemetry);

        verify(telemetryRepositoryPort, never())
                .save(any(Telemetry.class));

        verify(telemetryCachePort, never())
                .saveLatest(any(Telemetry.class));
    }
}