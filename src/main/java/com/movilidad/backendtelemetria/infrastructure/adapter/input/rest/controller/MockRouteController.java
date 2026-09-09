package com.movilidad.backendtelemetria.infrastructure.adapter.input.rest.controller;

import com.movilidad.backendtelemetria.infrastructure.adapter.output.route.RouteServiceResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class MockRouteController {

    @GetMapping("/mock-route")
    public RouteServiceResponse calculateRoute(
            @RequestParam double originLat,
            @RequestParam double originLng,
            @RequestParam double destinationLat,
            @RequestParam double destinationLng
    ) {

        double distance =
                calculateDistance(
                        originLat,
                        originLng,
                        destinationLat,
                        destinationLng
                );

        int estimatedMinutes =
                (int) Math.ceil(distance * 3);

        return new RouteServiceResponse(
                distance,
                estimatedMinutes
        );
    }

    private double calculateDistance(
            double originLat,
            double originLng,
            double destinationLat,
            double destinationLng
    ) {

        double latDifference =
                destinationLat - originLat;

        double lngDifference =
                destinationLng - originLng;

        return Math.sqrt(
                Math.pow(latDifference, 2)
                        +
                        Math.pow(lngDifference, 2)
        ) * 111;
    }
}