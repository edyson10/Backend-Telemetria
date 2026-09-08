package com.movilidad.backendtelemetria.infrastructure.exception;

import com.movilidad.backendtelemetria.domain.exception.InvalidTelemetryException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    @Test
    void shouldHandleInvalidTelemetry() {

        InvalidTelemetryException exception =
                new InvalidTelemetryException(
                        "Latitude must be between -90 and 90"
                );

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        when(request.getRequestURI())
                .thenReturn("/api/v1/telemetry");

        var response =
                handler.handleInvalidTelemetry(
                        exception,
                        request
                );

        assertEquals(
                400,
                response.getStatusCode().value()
        );

        assertEquals(
                "INVALID_TELEMETRY",
                response.getBody().code()
        );

        assertEquals(
                "/api/v1/telemetry",
                response.getBody().path()
        );
    }
}
