package com.movilidad.backendtelemetria.application.port.input;

import com.movilidad.backendtelemetria.application.port.output.RouteInformation;

public interface CalculateRouteUseCase {

    RouteInformation calculate(
            double originLatitude,
            double originLongitude,
            double destinationLatitude,
            double destinationLongitude
    );
}
