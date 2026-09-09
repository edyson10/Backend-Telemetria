package com.movilidad.backendtelemetria.application.service;

import com.movilidad.backendtelemetria.application.port.output.AlertRepositoryPort;
import com.movilidad.backendtelemetria.application.port.output.TelemetryCachePort;
import com.movilidad.backendtelemetria.application.port.output.TelemetryRepositoryPort;
import com.movilidad.backendtelemetria.domain.model.LatestVehicleTelemetry;
import com.movilidad.backendtelemetria.domain.model.Telemetry;
import com.movilidad.backendtelemetria.domain.model.TelemetryAlert;
import com.movilidad.backendtelemetria.domain.model.VehicleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelemetryApplicationServiceTest {

    private TelemetryRepositoryPort telemetryRepositoryPort;
    private TelemetryCachePort telemetryCachePort;
    private VehicleStopDetectionService vehicleStopDetectionService;
    private AlertRepositoryPort alertRepositoryPort;

    private TelemetryApplicationService service;

    @BeforeEach
    void setUp() {

        telemetryRepositoryPort =
                mock(TelemetryRepositoryPort.class);

        telemetryCachePort =
                mock(TelemetryCachePort.class);

        vehicleStopDetectionService =
                new VehicleStopDetectionService();

        alertRepositoryPort =
                mock(AlertRepositoryPort.class);

        service = new TelemetryApplicationService(
                telemetryRepositoryPort,
                telemetryCachePort,
                vehicleStopDetectionService,
                alertRepositoryPort
        );
    }

    @Test
    void shouldAcceptNewTelemetry() {

        Telemetry telemetry = Telemetry.create(
                "VH-001",
                6.2442,
                -75.5812,
                Instant.parse("2026-09-08T13:00:00Z")
        );

        when(telemetryCachePort.registerIfNotDuplicate(telemetry))
                .thenReturn(true);

        when(telemetryCachePort.findLatest("VH-001"))
                .thenReturn(Optional.empty());

        TelemetryIngestionResult result =
                service.ingest(telemetry);

        assertFalse(result.duplicate());

        assertEquals(
                VehicleStatus.MOVING,
                result.status()
        );

        assertEquals(
                telemetry,
                result.telemetry()
        );

        verify(telemetryRepositoryPort)
                .save(telemetry);

        verify(telemetryCachePort)
                .saveLatest(
                        telemetry,
                        VehicleStatus.MOVING
                );
    }

    @Test
    void shouldReturnDuplicateWhenTelemetryWasAlreadyRegistered() {

        Telemetry telemetry = Telemetry.create(
                "VH-001",
                6.2442,
                -75.5812,
                Instant.parse("2026-09-08T13:00:00Z")
        );

        when(telemetryCachePort.registerIfNotDuplicate(telemetry))
                .thenReturn(false);

        when(telemetryCachePort.findLatest("VH-001"))
                .thenReturn(Optional.empty());

        TelemetryIngestionResult result =
                service.ingest(telemetry);

        assertTrue(result.duplicate());

        assertEquals(
                VehicleStatus.MOVING,
                result.status()
        );

        verify(telemetryRepositoryPort, never())
                .save(any());

        verify(telemetryCachePort, never())
                .saveLatest(
                        any(),
                        any()
                );
    }

    @Test
    void shouldDetectStoppedVehicle() {

        Telemetry previousTelemetry = Telemetry.create(
                "VH-001",
                6.2442,
                -75.5812,
                Instant.parse("2026-09-08T13:00:00Z")
        );

        Telemetry currentTelemetry = Telemetry.create(
                "VH-001",
                6.2442,
                -75.5812,
                Instant.parse("2026-09-08T13:01:01Z")
        );

        LatestVehicleTelemetry previousLatest =
                toLatestVehicleTelemetry(
                        previousTelemetry,
                        VehicleStatus.MOVING
                );

        when(telemetryCachePort.registerIfNotDuplicate(currentTelemetry))
                .thenReturn(true);

        when(telemetryCachePort.findLatest("VH-001"))
                .thenReturn(Optional.of(previousLatest));

        TelemetryIngestionResult result =
                service.ingest(currentTelemetry);

        assertFalse(result.duplicate());

        assertEquals(
                VehicleStatus.STOPPED,
                result.status()
        );

        verify(telemetryRepositoryPort)
                .save(currentTelemetry);

        verify(telemetryCachePort)
                .saveLatest(
                        currentTelemetry,
                        VehicleStatus.STOPPED
                );
    }

    @Test
    void shouldDetectMovingVehicleWhenPositionChanges() {

        Telemetry previousTelemetry = Telemetry.create(
                "VH-001",
                6.2442,
                -75.5812,
                Instant.parse("2026-09-08T13:00:00Z")
        );

        Telemetry currentTelemetry = Telemetry.create(
                "VH-001",
                6.2500,
                -75.5900,
                Instant.parse("2026-09-08T13:01:01Z")
        );

        LatestVehicleTelemetry previousLatest =
                toLatestVehicleTelemetry(
                        previousTelemetry,
                        VehicleStatus.MOVING
                );

        when(telemetryCachePort.registerIfNotDuplicate(currentTelemetry))
                .thenReturn(true);

        when(telemetryCachePort.findLatest("VH-001"))
                .thenReturn(Optional.of(previousLatest));

        TelemetryIngestionResult result =
                service.ingest(currentTelemetry);

        assertEquals(
                VehicleStatus.MOVING,
                result.status()
        );

        verify(telemetryCachePort)
                .saveLatest(
                        currentTelemetry,
                        VehicleStatus.MOVING
                );
    }

    @Test
    void shouldGenerateAlertWhenVehicleIsStopped() {

        Telemetry previousTelemetry = Telemetry.create(
                "VH-001",
                6.2442,
                -75.5812,
                Instant.parse("2026-09-08T13:00:00Z")
        );

        Telemetry currentTelemetry = Telemetry.create(
                "VH-001",
                6.2442,
                -75.5812,
                Instant.parse("2026-09-08T13:01:01Z")
        );

        LatestVehicleTelemetry previousLatest =
                toLatestVehicleTelemetry(
                        previousTelemetry,
                        VehicleStatus.MOVING
                );

        when(telemetryCachePort.registerIfNotDuplicate(currentTelemetry))
                .thenReturn(true);

        when(telemetryCachePort.findLatest("VH-001"))
                .thenReturn(Optional.of(previousLatest));

        TelemetryIngestionResult result =
                service.ingest(currentTelemetry);

        assertEquals(
                VehicleStatus.STOPPED,
                result.status()
        );

        verify(alertRepositoryPort)
                .save(any(TelemetryAlert.class));

        verify(telemetryRepositoryPort)
                .save(currentTelemetry);

        verify(telemetryCachePort)
                .saveLatest(
                        currentTelemetry,
                        VehicleStatus.STOPPED
                );
    }

    @Test
    void shouldNotGenerateAlertWhenVehicleIsMoving() {

        Telemetry previousTelemetry = Telemetry.create(
                "VH-001",
                6.2442,
                -75.5812,
                Instant.parse("2026-09-08T13:00:00Z")
        );

        Telemetry currentTelemetry = Telemetry.create(
                "VH-001",
                6.2500,
                -75.5900,
                Instant.parse("2026-09-08T13:01:01Z")
        );

        LatestVehicleTelemetry previousLatest =
                toLatestVehicleTelemetry(
                        previousTelemetry,
                        VehicleStatus.MOVING
                );

        when(telemetryCachePort.registerIfNotDuplicate(currentTelemetry))
                .thenReturn(true);

        when(telemetryCachePort.findLatest("VH-001"))
                .thenReturn(Optional.of(previousLatest));

        TelemetryIngestionResult result =
                service.ingest(currentTelemetry);

        assertEquals(
                VehicleStatus.MOVING,
                result.status()
        );

        verify(alertRepositoryPort, never())
                .save(any(TelemetryAlert.class));

        verify(telemetryCachePort)
                .saveLatest(
                        currentTelemetry,
                        VehicleStatus.MOVING
                );
    }

    private LatestVehicleTelemetry toLatestVehicleTelemetry(
            Telemetry telemetry,
            VehicleStatus status
    ) {
        return new LatestVehicleTelemetry(
                telemetry.getVehicleId(),
                telemetry.getLatitude(),
                telemetry.getLongitude(),
                telemetry.getTimestamp(),
                status
        );
    }
}