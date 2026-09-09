package com.movilidad.backendtelemetria.infrastructure.adapter.input.rest.controller;

import com.movilidad.backendtelemetria.application.port.input.IngestTelemetryUseCase;
import com.movilidad.backendtelemetria.application.service.TelemetryIngestionResult;
import com.movilidad.backendtelemetria.domain.model.Telemetry;
import com.movilidad.backendtelemetria.infrastructure.adapter.input.rest.request.TelemetryRequest;
import com.movilidad.backendtelemetria.infrastructure.adapter.input.rest.mapper.TelemetryRestMapper;
import com.movilidad.backendtelemetria.infrastructure.adapter.input.rest.response.TelemetryResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/telemetry")
public class TelemetryController {

    private final IngestTelemetryUseCase ingestTelemetryUseCase;
    private final TelemetryRestMapper telemetryRestMapper;

    public TelemetryController(
            IngestTelemetryUseCase ingestTelemetryUseCase,
            TelemetryRestMapper telemetryRestMapper
    ) {
        this.ingestTelemetryUseCase = ingestTelemetryUseCase;
        this.telemetryRestMapper = telemetryRestMapper;
    }

    @PostMapping
    public ResponseEntity<TelemetryResponse> ingest(
            @Valid @RequestBody TelemetryRequest request
    ) {

        Telemetry telemetry =
                telemetryRestMapper.toDomain(request);

        TelemetryIngestionResult result =
                ingestTelemetryUseCase.ingest(telemetry);

        TelemetryResponse response =
                telemetryRestMapper.toResponse(
                        result.telemetry(),
                        result.status()
                );

        if (result.duplicate()) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(response);
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
