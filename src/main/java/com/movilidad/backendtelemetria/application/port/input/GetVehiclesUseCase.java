package com.movilidad.backendtelemetria.application.port.input;

import com.movilidad.backendtelemetria.domain.model.VehicleSnapshot;

import java.util.List;

public interface GetVehiclesUseCase {
    List<VehicleSnapshot> getVehicles();
}
