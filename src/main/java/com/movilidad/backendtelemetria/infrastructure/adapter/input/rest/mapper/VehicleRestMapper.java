package com.movilidad.backendtelemetria.infrastructure.adapter.input.rest.mapper;

import com.movilidad.backendtelemetria.domain.model.VehicleSnapshot;
import com.movilidad.backendtelemetria.infrastructure.adapter.input.rest.response.VehicleResponse;
import org.springframework.stereotype.Component;

@Component
public class VehicleRestMapper {

    public VehicleResponse toResponse(VehicleSnapshot vehicle) {
        return new VehicleResponse(
                vehicle.vehicleId(),
                vehicle.latitude(),
                vehicle.longitude(),
                vehicle.timestamp(),
                vehicle.status().name()
        );
    }
}
