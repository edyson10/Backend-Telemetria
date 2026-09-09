package com.movilidad.backendtelemetria.application.service;

import com.movilidad.backendtelemetria.application.port.input.CalculateRouteUseCase;
import com.movilidad.backendtelemetria.application.port.output.RouteInformation;
import com.movilidad.backendtelemetria.application.port.output.RouteServicePort;

public class RouteCalculationService implements CalculateRouteUseCase {

    private final RouteServicePort routeServicePort;

    public RouteCalculationService(
            RouteServicePort routeServicePort
    ) {
        this.routeServicePort = routeServicePort;
    }

    public RouteInformation calculateRoute(
            double originLatitude,
            double originLongitude,
            double destinationLatitude,
            double destinationLongitude
    ) {
        return routeServicePort.calculateRoute(
                originLatitude,
                originLongitude,
                destinationLatitude,
                destinationLongitude
        );
    }

    @Override
    public RouteInformation calculate(double originLatitude, double originLongitude, double destinationLatitude, double destinationLongitude) {
        return routeServicePort.calculateRoute(
                originLatitude,
                originLongitude,
                destinationLatitude,
                destinationLongitude
        );
    }
}
