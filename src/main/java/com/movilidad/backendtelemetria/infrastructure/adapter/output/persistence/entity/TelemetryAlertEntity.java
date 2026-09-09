package com.movilidad.backendtelemetria.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "telemetry_alert")
public class TelemetryAlertEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id", nullable = false)
    private String vehicleId;

    @Column(name = "alert_type", nullable = false)
    private String alertType;

    @Column(nullable = false)
    private String message;

    @Column(name = "alert_timestamp", nullable = false)
    private Instant alertTimestamp;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected TelemetryAlertEntity() {
    }

    public TelemetryAlertEntity(
            String vehicleId,
            String alertType,
            String message,
            Instant alertTimestamp
    ) {
        this.vehicleId = vehicleId;
        this.alertType = alertType;
        this.message = message;
        this.alertTimestamp = alertTimestamp;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public String getAlertType() {
        return alertType;
    }

    public String getMessage() {
        return message;
    }

    public Instant getAlertTimestamp() {
        return alertTimestamp;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
