package com.movilidad.backendtelemetria.infrastructure.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Fleet Telemetry API",
                version = "1.0.0",
                description = "API for telemetry ingestion, vehicle monitoring, alerts and route calculation.",
                contact = @Contact(name = "Movilidad Telemetry")
        )
)
public class OpenApiConfiguration {
}
