package com.movilidad.backendtelemetria.application.service;

import com.movilidad.backendtelemetria.domain.model.Telemetry;
import com.movilidad.backendtelemetria.domain.model.VehicleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VehicleStopDetectionServiceTest {

    private VehicleStopDetectionService service;

    @BeforeEach
    void setUp() {
        service = new VehicleStopDetectionService();
    }

    @Test
    void shouldReturnMovingWhenThereIsNoPreviousTelemetry() {

        Telemetry current = telemetry(
                "2026-09-08T13:01:00Z",
                6.2442,
                -75.5812
        );

        VehicleStatus result =
                service.evaluate(current, Optional.empty());

        assertEquals(
                VehicleStatus.MOVING,
                result
        );
    }

    @Test
    void shouldReturnMovingWhenPositionChanges() {

        Telemetry previous = telemetry(
                "2026-09-08T13:00:00Z",
                6.2442,
                -75.5812
        );

        Telemetry current = telemetry(
                "2026-09-08T13:01:30Z",
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
    }

    @Test
    void shouldReturnMovingAtExactlyOneMinute() {

        Telemetry previous = telemetry(
                "2026-09-08T13:00:00Z",
                6.2442,
                -75.5812
        );

        Telemetry current = telemetry(
                "2026-09-08T13:01:00Z",
                6.2442,
                -75.5812
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
    void shouldReturnStoppedWhenPositionRemainsForMoreThanOneMinute() {

        Telemetry previous = telemetry(
                "2026-09-08T13:00:00Z",
                6.2442,
                -75.5812
        );

        Telemetry current = telemetry(
                "2026-09-08T13:01:01Z",
                6.2442,
                -75.5812
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
