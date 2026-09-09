package com.movilidad.backendtelemetria.application.service;

import com.movilidad.backendtelemetria.domain.model.Telemetry;
import com.movilidad.backendtelemetria.domain.model.VehicleStatus;

import java.time.Duration;
import java.util.Optional;

public class VehicleStopDetectionService {

    private static final Duration STOP_THRESHOLD =
            Duration.ofMinutes(1);

    public VehicleStatus evaluate(
            Telemetry current,
            Optional<Telemetry> previous
    ) {

        if (previous.isEmpty()) {
            return VehicleStatus.MOVING;
        }

        Telemetry previousTelemetry = previous.get();

        boolean samePosition =
                Double.compare(
                        current.getLatitude(),
                        previousTelemetry.getLatitude()
                ) == 0
                        &&
                        Double.compare(
                                current.getLongitude(),
                                previousTelemetry.getLongitude()
                        ) == 0;

        if (!samePosition) {
            return VehicleStatus.MOVING;
        }

        Duration elapsed = Duration.between(
                previousTelemetry.getTimestamp(),
                current.getTimestamp()
        );

        if (elapsed.compareTo(STOP_THRESHOLD) > 0) {
            return VehicleStatus.STOPPED;
        }

        return VehicleStatus.MOVING;
    }
}
