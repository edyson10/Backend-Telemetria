package com.movilidad.backendtelemetria.infrastructure.adapter.input.rest;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record TelemetryRequest(

        @NotBlank(
                message = "vehicleId is required"
        )
        String vehicleId,

        @NotNull(
                message = "lat is required"
        )
        @DecimalMin(
                value = "-90.0",
                message = "lat must be greater than or equal to -90"
        )
        @DecimalMax(
                value = "90.0",
                message = "lat must be less than or equal to 90"
        )
        Double lat,

        @NotNull(
                message = "lng is required"
        )
        @DecimalMin(
                value = "-180.0",
                message = "lng must be greater than or equal to -180"
        )
        @DecimalMax(
                value = "180.0",
                message = "lng must be less than or equal to 180"
        )
        Double lng,

        @NotNull(
                message = "timestamp is required"
        )
        Instant timestamp
) {
}
