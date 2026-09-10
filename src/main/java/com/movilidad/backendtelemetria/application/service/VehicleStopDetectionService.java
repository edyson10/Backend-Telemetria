package com.movilidad.backendtelemetria.application.service;

import com.movilidad.backendtelemetria.application.port.output.TelemetryCachePort;
import com.movilidad.backendtelemetria.domain.model.Telemetry;
import com.movilidad.backendtelemetria.domain.model.VehicleStatus;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public class VehicleStopDetectionService {

    private static final Duration STOP_THRESHOLD =
            Duration.ofMinutes(1);

    private final TelemetryCachePort telemetryCachePort;

    public VehicleStopDetectionService(
            TelemetryCachePort telemetryCachePort
    ) {
        this.telemetryCachePort = telemetryCachePort;
    }

    public VehicleStatus evaluate(
            Telemetry current,
            Optional<Telemetry> previous
    ) {

        if (previous.isEmpty()) {
            telemetryCachePort.saveStopStart(
                    current.getVehicleId(),
                    current.getTimestamp()
            );

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
            telemetryCachePort.deleteStopStart(
                    current.getVehicleId()
            );

            return VehicleStatus.MOVING;
        }

        Optional<Instant> stopStart =
                telemetryCachePort.findStopStart(
                        current.getVehicleId()
                );

        if (stopStart.isEmpty()) {
            telemetryCachePort.saveStopStart(
                    current.getVehicleId(),
                    previousTelemetry.getTimestamp()
            );

            return VehicleStatus.MOVING;
        }

        Duration elapsed = Duration.between(
                stopStart.get(),
                current.getTimestamp()
        );

        if (elapsed.compareTo(STOP_THRESHOLD) > 0) {
            return VehicleStatus.STOPPED;
        }

        return VehicleStatus.MOVING;
    }
}