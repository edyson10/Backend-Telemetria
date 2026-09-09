package com.movilidad.backendtelemetria.application.service;

import com.movilidad.backendtelemetria.application.port.output.TelemetryCachePort;
import com.movilidad.backendtelemetria.domain.model.Telemetry;
import com.movilidad.backendtelemetria.domain.model.VehicleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class VehicleStopDetectionServiceTest {

    private TelemetryCachePort telemetryCachePort;
    private VehicleStopDetectionService service;

    @BeforeEach
    void setUp() {
        telemetryCachePort = mock(TelemetryCachePort.class);

        service = new VehicleStopDetectionService(
                telemetryCachePort
        );
    }

    @Test
    void shouldReturnMovingAndStartTrackingWhenThereIsNoPreviousTelemetry() {

        Telemetry current = telemetry(
                "2026-09-08T13:00:00Z",
                6.2442,
                -75.5812
        );

        VehicleStatus result =
                service.evaluate(
                        current,
                        Optional.empty()
                );

        assertEquals(
                VehicleStatus.MOVING,
                result
        );

        verify(telemetryCachePort)
                .saveStopStart(
                        "VH-001",
                        current.getTimestamp()
                );
    }

    @Test
    void shouldReturnMovingAndResetStopTrackingWhenPositionChanges() {

        Telemetry previous = telemetry(
                "2026-09-08T13:00:00Z",
                6.2442,
                -75.5812
        );

        Telemetry current = telemetry(
                "2026-09-08T13:00:05Z",
                6.2443,
                -75.5813
        );

        VehicleStatus result =
                service.evaluate(
                        current,
                        Optional.of(previous)
                );

        assertEquals(
                VehicleStatus.MOVING,
                result
        );

        verify(telemetryCachePort)
                .deleteStopStart("VH-001");
    }

    @Test
    void shouldReturnMovingWhenVehicleHasBeenStoppedForLessThanOneMinute() {

        Telemetry previous = telemetry(
                "2026-09-08T13:00:05Z",
                6.2442,
                -75.5812
        );

        Telemetry current = telemetry(
                "2026-09-08T13:00:10Z",
                6.2442,
                -75.5812
        );

        when(telemetryCachePort.findStopStart("VH-001"))
                .thenReturn(
                        Optional.of(
                                Instant.parse(
                                        "2026-09-08T13:00:00Z"
                                )
                        )
                );

        VehicleStatus result =
                service.evaluate(
                        current,
                        Optional.of(previous)
                );

        assertEquals(
                VehicleStatus.MOVING,
                result
        );
    }

    @Test
    void shouldReturnMovingWhenVehicleHasBeenStoppedForExactlyOneMinute() {

        Telemetry previous = telemetry(
                "2026-09-08T13:00:55Z",
                6.2442,
                -75.5812
        );

        Telemetry current = telemetry(
                "2026-09-08T13:01:00Z",
                6.2442,
                -75.5812
        );

        when(telemetryCachePort.findStopStart("VH-001"))
                .thenReturn(
                        Optional.of(
                                Instant.parse(
                                        "2026-09-08T13:00:00Z"
                                )
                        )
                );

        VehicleStatus result =
                service.evaluate(
                        current,
                        Optional.of(previous)
                );

        assertEquals(
                VehicleStatus.MOVING,
                result
        );
    }

    @Test
    void shouldReturnStoppedWhenVehicleRemainsInSamePositionForMoreThanOneMinute() {

        Telemetry previous = telemetry(
                "2026-09-08T13:01:00Z",
                6.2442,
                -75.5812
        );

        Telemetry current = telemetry(
                "2026-09-08T13:01:05Z",
                6.2442,
                -75.5812
        );

        when(telemetryCachePort.findStopStart("VH-001"))
                .thenReturn(
                        Optional.of(
                                Instant.parse(
                                        "2026-09-08T13:00:00Z"
                                )
                        )
                );

        VehicleStatus result =
                service.evaluate(
                        current,
                        Optional.of(previous)
                );

        assertEquals(
                VehicleStatus.STOPPED,
                result
        );
    }

    @Test
    void shouldContinueTrackingWhenStopStartIsNotPresent() {

        Telemetry previous = telemetry(
                "2026-09-08T13:00:05Z",
                6.2442,
                -75.5812
        );

        Telemetry current = telemetry(
                "2026-09-08T13:00:10Z",
                6.2442,
                -75.5812
        );

        when(telemetryCachePort.findStopStart("VH-001"))
                .thenReturn(Optional.empty());

        VehicleStatus result =
                service.evaluate(
                        current,
                        Optional.of(previous)
                );

        assertEquals(
                VehicleStatus.MOVING,
                result
        );

        verify(telemetryCachePort)
                .saveStopStart(
                        "VH-001",
                        previous.getTimestamp()
                );
    }

    private Telemetry telemetry(
            String timestamp,
            double latitude,
            double longitude
    ) {
        return Telemetry.create(
                "VH-001",
                latitude,
                longitude,
                Instant.parse(timestamp)
        );
    }
}