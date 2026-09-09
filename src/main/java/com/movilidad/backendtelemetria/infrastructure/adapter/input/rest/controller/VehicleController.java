package com.movilidad.backendtelemetria.infrastructure.adapter.input.rest.controller;

import com.movilidad.backendtelemetria.application.port.input.GetVehiclesUseCase;
import com.movilidad.backendtelemetria.infrastructure.adapter.input.rest.mapper.VehicleRestMapper;
import com.movilidad.backendtelemetria.infrastructure.adapter.input.rest.response.VehicleResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicles")
public class VehicleController {

    private final GetVehiclesUseCase getVehiclesUseCase;
    private final VehicleRestMapper vehicleRestMapper;

    public VehicleController(
            GetVehiclesUseCase getVehiclesUseCase,
            VehicleRestMapper vehicleRestMapper
    ) {
        this.getVehiclesUseCase = getVehiclesUseCase;
        this.vehicleRestMapper = vehicleRestMapper;
    }

    @GetMapping
    public List<VehicleResponse> getVehicles() {

        return getVehiclesUseCase.getVehicles()
                .stream()
                .map(vehicleRestMapper::toResponse)
                .toList();
    }
}
