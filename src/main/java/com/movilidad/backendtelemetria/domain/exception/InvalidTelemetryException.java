package com.movilidad.backendtelemetria.domain.exception;

public class InvalidTelemetryException extends RuntimeException {

    public InvalidTelemetryException(String message) {
        super(message);
    }
}
