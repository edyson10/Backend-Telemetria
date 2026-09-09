package com.movilidad.backendtelemetria.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TelemetryAlertTest {

    @Test
    void shouldCreateVehicleStoppedAlert() {

        Instant timestamp =
                Instant.parse("2026-09-08T13:01:01Z");

        TelemetryAlert alert =
                TelemetryAlert.vehicleStopped(
                        "VH-001",
                        timestamp
                );

        assertEquals(
                "VH-001",
                alert.getVehicleId()
        );

        assertEquals(
                "VEHICLE_STOPPED",
                alert.getType()
        );

        assertEquals(
                "Vehículo Detenido",
                alert.getMessage()
        );

        assertEquals(
                timestamp,
                alert.getTimestamp()
        );
    }
}
