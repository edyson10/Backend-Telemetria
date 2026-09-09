package com.movilidad.backendtelemetria.infrastructure.adapter.input.rest.controller;

import com.movilidad.backendtelemetria.application.port.input.CalculateRouteUseCase;
import com.movilidad.backendtelemetria.application.port.output.RouteInformation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/routes")
public class RouteController {

    private final CalculateRouteUseCase calculateRouteUseCase;

    public RouteController(
            CalculateRouteUseCase calculateRouteUseCase
    ) {
        this.calculateRouteUseCase =
                calculateRouteUseCase;
    }

    @GetMapping
    public RouteInformation calculate(
            @RequestParam double originLat,
            @RequestParam double originLng,
            @RequestParam double destinationLat,
            @RequestParam double destinationLng
    ) {

        return calculateRouteUseCase.calculate(
                originLat,
                originLng,
                destinationLat,
                destinationLng
        );
    }
}
