package com.movilidad.backendtelemetria.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VehicleTest {

    @Test
    void shouldCreateVehicleWithMovingStatus() {

        Vehicle vehicle = Vehicle.create(
                "VH-001",
                "Vehicle 001"
        );

        assertEquals(
                "VH-001",
                vehicle.getId()
        );

        assertEquals(
                "Vehicle 001",
                vehicle.getName()
        );

        assertEquals(
                VehicleStatus.MOVING,
                vehicle.getStatus()
        );
    }

    @Test
    void shouldChangeVehicleToStopped() {

        Vehicle vehicle = Vehicle.create(
                "VH-001",
                "Vehicle 001"
        );

        vehicle.markAsStopped();

        assertEquals(
                VehicleStatus.STOPPED,
                vehicle.getStatus()
        );
    }

    @Test
    void shouldChangeVehicleToAlert() {

        Vehicle vehicle = Vehicle.create(
                "VH-001",
                "Vehicle 001"
        );

        vehicle.markAsAlert();

        assertEquals(
                VehicleStatus.ALERT,
                vehicle.getStatus()
        );
    }

    @Test
    void shouldChangeVehicleBackToMoving() {

        Vehicle vehicle = Vehicle.create(
                "VH-001",
                "Vehicle 001"
        );

        vehicle.markAsStopped();
        vehicle.markAsMoving();

        assertEquals(
                VehicleStatus.MOVING,
                vehicle.getStatus()
        );
    }
}
