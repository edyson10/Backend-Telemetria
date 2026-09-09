package com.movilidad.backendtelemetria.application.port.output;

public interface RouteServicePort {

    RouteInformation calculateRoute(
            double originLatitude,
            double originLongitude,
            double destinationLatitude,
            double destinationLongitude
    );
}
