package com.movilidad.backendtelemetria.infrastructure.adapter.output.route;

import com.movilidad.backendtelemetria.application.exception.RouteServiceException;
import com.movilidad.backendtelemetria.application.port.output.RouteInformation;
import com.movilidad.backendtelemetria.application.port.output.RouteServicePort;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.function.Supplier;

@Component
public class RouteServiceAdapter implements RouteServicePort {

    private final RestClient restClient;
    private final CircuitBreaker circuitBreaker;

    public RouteServiceAdapter(
            RestClient.Builder restClientBuilder,
            CircuitBreakerRegistry circuitBreakerRegistry,
            RouteServiceProperties properties
    ) {
        this.restClient =
                restClientBuilder
                        .baseUrl(properties.baseUrl())
                        .build();

        this.circuitBreaker =
                circuitBreakerRegistry
                        .circuitBreaker("route-service");
    }

    @Override
    public RouteInformation calculateRoute(
            double originLatitude,
            double originLongitude,
            double destinationLatitude,
            double destinationLongitude
    ) {

        Supplier<RouteInformation> supplier =
                CircuitBreaker.decorateSupplier(
                        circuitBreaker,
                        () -> callExternalService(
                                originLatitude,
                                originLongitude,
                                destinationLatitude,
                                destinationLongitude
                        )
                );

        try {
            return supplier.get();

        } catch (CallNotPermittedException exception) {

            return fallback();

        } catch (RouteServiceException exception) {

            return fallback();
        }
    }

    private RouteInformation callExternalService(
            double originLatitude,
            double originLongitude,
            double destinationLatitude,
            double destinationLongitude
    ) {

        try {

            RouteServiceResponse response =
                    restClient.get()
                            .uri(uriBuilder -> uriBuilder
                                    .path("/api/v1/mock-route")
                                    .queryParam(
                                            "originLat",
                                            originLatitude
                                    )
                                    .queryParam(
                                            "originLng",
                                            originLongitude
                                    )
                                    .queryParam(
                                            "destinationLat",
                                            destinationLatitude
                                    )
                                    .queryParam(
                                            "destinationLng",
                                            destinationLongitude
                                    )
                                    .build()
                            )
                            .retrieve()
                            .body(RouteServiceResponse.class);

            if (response == null) {
                throw new RouteServiceException(
                        "Route service returned an empty response"
                );
            }

            return new RouteInformation(
                    response.distanceKm(),
                    response.estimatedMinutes()
            );

        } catch (Exception exception) {

            if (exception instanceof RouteServiceException) {
                throw exception;
            }

            throw new RouteServiceException(
                    "Route service is unavailable",
                    exception
            );
        }
    }

    private RouteInformation fallback() {

        return new RouteInformation(
                0.0,
                0
        );
    }
}