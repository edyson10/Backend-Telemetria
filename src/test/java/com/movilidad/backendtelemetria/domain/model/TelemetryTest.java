package com.movilidad.backendtelemetria.domain.model;

import com.movilidad.backendtelemetria.domain.exception.InvalidTelemetryException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TelemetryTest {

    @Test
    void shouldCreateValidTelemetry() {

        Instant timestamp = Instant.now();

        Telemetry telemetry = Telemetry.create(
                "VH-001",
                6.2442,
                -75.5812,
                timestamp
        );

        assertEquals(
                "VH-001",
                telemetry.getVehicleId()
        );

        assertEquals(
                6.2442,
                telemetry.getLatitude()
        );

        assertEquals(
                -75.5812,
                telemetry.getLongitude()
        );

        assertEquals(
                timestamp,
                telemetry.getTimestamp()
        );
    }

    @Test
    void shouldRejectInvalidLatitude() {

        assertThrows(
                InvalidTelemetryException.class,
                () -> Telemetry.create(
                        "VH-001",
                        100,
                        -75.5812,
                        Instant.now()
                )
        );
    }

    @Test
    void shouldRejectInvalidLongitude() {

        assertThrows(
                InvalidTelemetryException.class,
                () -> Telemetry.create(
                        "VH-001",
                        6.2442,
                        200,
                        Instant.now()
                )
        );
    }

    @Test
    void shouldRejectBlankVehicleId() {

        assertThrows(
                InvalidTelemetryException.class,
                () -> Telemetry.create(
                        "",
                        6.2442,
                        -75.5812,
                        Instant.now()
                )
        );
    }

    @Test
    void shouldRejectFutureTimestamp() {

        Instant futureTimestamp =
                Instant.now().plusSeconds(60);

        assertThrows(
                InvalidTelemetryException.class,
                () -> Telemetry.create(
                        "VH-001",
                        6.2442,
                        -75.5812,
                        futureTimestamp
                )
        );
    }
}
