package com.movilidad.backendtelemetria.application.service;

import com.movilidad.backendtelemetria.application.port.output.TelemetryCachePort;
import com.movilidad.backendtelemetria.application.port.output.TelemetryRepositoryPort;
import com.movilidad.backendtelemetria.domain.model.LatestVehicleTelemetry;
import com.movilidad.backendtelemetria.domain.model.Telemetry;
import com.movilidad.backendtelemetria.domain.model.VehicleSnapshot;
import com.movilidad.backendtelemetria.domain.model.VehicleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class VehicleQueryServiceTest {

    private TelemetryRepositoryPort telemetryRepositoryPort;
    private TelemetryCachePort telemetryCachePort;

    private VehicleQueryService service;

    @BeforeEach
    void setUp() {

        telemetryRepositoryPort =
                mock(TelemetryRepositoryPort.class);

        telemetryCachePort =
                mock(TelemetryCachePort.class);

        service = new VehicleQueryService(
                telemetryRepositoryPort,
                telemetryCachePort
        );
    }

    @Test
    void shouldReturnVehiclesFromCache() {

        LatestVehicleTelemetry latest =
                new LatestVehicleTelemetry(
                        "VH-001",
                        6.2442,
                        -75.5812,
                        Instant.parse("2026-09-08T15:02:02Z"),
                        VehicleStatus.STOPPED
                );

        when(telemetryRepositoryPort.findVehicleIds())
                .thenReturn(List.of("VH-001"));

        when(telemetryCachePort.findLatest("VH-001"))
                .thenReturn(Optional.of(latest));

        List<VehicleSnapshot> result =
                service.getVehicles();

        assertEquals(1, result.size());

        VehicleSnapshot snapshot =
                result.getFirst();

        assertEquals("VH-001", snapshot.vehicleId());
        assertEquals(6.2442, snapshot.latitude());
        assertEquals(-75.5812, snapshot.longitude());
        assertEquals(
                VehicleStatus.STOPPED,
                snapshot.status()
        );

        verify(telemetryCachePort)
                .findLatest("VH-001");

        verify(telemetryRepositoryPort)
                .findVehicleIds();
    }

    @Test
    void shouldUseDatabaseWhenCacheIsEmpty() {

        Telemetry telemetry =
                Telemetry.create(
                        "VH-001",
                        6.2442,
                        -75.5812,
                        Instant.parse("2026-09-08T15:02:02Z")
                );

        when(telemetryRepositoryPort.findVehicleIds())
                .thenReturn(List.of("VH-001"));

        when(telemetryCachePort.findLatest("VH-001"))
                .thenReturn(Optional.empty());

        when(telemetryRepositoryPort.findLatestByVehicleId("VH-001"))
                .thenReturn(Optional.of(telemetry));

        List<VehicleSnapshot> result =
                service.getVehicles();

        assertEquals(1, result.size());

        VehicleSnapshot snapshot =
                result.getFirst();

        assertEquals("VH-001", snapshot.vehicleId());
        assertEquals(
                VehicleStatus.MOVING,
                snapshot.status()
        );

        verify(telemetryRepositoryPort)
                .findLatestByVehicleId("VH-001");
    }

    @Test
    void shouldReturnEmptyListWhenThereAreNoVehicles() {

        when(telemetryRepositoryPort.findVehicleIds())
                .thenReturn(List.of());

        List<VehicleSnapshot> result =
                service.getVehicles();

        assertTrue(result.isEmpty());

        verify(telemetryCachePort, never())
                .findLatest(anyString());
    }

    @Test
    void shouldFailWhenVehicleHasNoTelemetry() {

        when(telemetryRepositoryPort.findVehicleIds())
                .thenReturn(List.of("VH-001"));

        when(telemetryCachePort.findLatest("VH-001"))
                .thenReturn(Optional.empty());

        when(telemetryRepositoryPort.findLatestByVehicleId("VH-001"))
                .thenReturn(Optional.empty());

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> service.getVehicles()
                );

        assertTrue(
                exception.getMessage()
                        .contains("Telemetry not found for vehicle")
        );
    }
}
