package com.movilidad.backendtelemetria.infrastructure.adapter.output.persistence;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "telemetry")
public class TelemetryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "vehicle_id",
            nullable = false,
            length = 100
    )
    private String vehicleId;

    @Column(
            name = "latitude",
            nullable = false
    )
    private double latitude;

    @Column(
            name = "longitude",
            nullable = false
    )
    private double longitude;

    @Column(
            name = "telemetry_timestamp",
            nullable = false
    )
    private Instant timestamp;

    protected TelemetryEntity() {
        // Required by JPA
    }

    public TelemetryEntity(
            String vehicleId,
            double latitude,
            double longitude,
            Instant timestamp) {

        this.vehicleId = vehicleId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
