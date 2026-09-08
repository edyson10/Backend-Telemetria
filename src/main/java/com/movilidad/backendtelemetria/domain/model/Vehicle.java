package com.movilidad.backendtelemetria.domain.model;

import java.util.Objects;

public final class Vehicle {

    private final String id;
    private final String name;

    private VehicleStatus status;

    private Vehicle(
            String id,
            String name,
            VehicleStatus status) {

        this.id = id;
        this.name = name;
        this.status = status;
    }

    public static Vehicle create(
            String id,
            String name) {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Vehicle ID cannot be null or blank"
            );
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Vehicle name cannot be null or blank"
            );
        }

        return new Vehicle(
                id,
                name,
                VehicleStatus.MOVING
        );
    }

    public void markAsMoving() {
        this.status = VehicleStatus.MOVING;
    }

    public void markAsStopped() {
        this.status = VehicleStatus.STOPPED;
    }

    public void markAsAlert() {
        this.status = VehicleStatus.ALERT;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof Vehicle vehicle)) {
            return false;
        }

        return Objects.equals(id, vehicle.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
