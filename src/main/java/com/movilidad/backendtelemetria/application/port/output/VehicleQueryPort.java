package com.movilidad.backendtelemetria.application.port.output;

import java.util.List;

public interface VehicleQueryPort {
    List<String> findVehicleIds();
}
