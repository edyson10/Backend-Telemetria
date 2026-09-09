package com.movilidad.backendtelemetria.infrastructure.adapter.output.route;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "route-service")
public record RouteServiceProperties(
        String baseUrl
) {
}
