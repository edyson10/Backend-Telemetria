package com.movilidad.backendtelemetria.infrastructure.adapter.input.rest;

import com.movilidad.backendtelemetria.application.port.input.IngestTelemetryUseCase;
import com.movilidad.backendtelemetria.application.service.TelemetryIngestionResult;
import com.movilidad.backendtelemetria.domain.model.Telemetry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TelemetryController.class)
@Import(TelemetryRestMapper.class)
class TelemetryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IngestTelemetryUseCase ingestTelemetryUseCase;

    @Test
    void shouldIngestValidTelemetry() throws Exception {

        Telemetry telemetry = Telemetry.create(
                "VH-001",
                6.2442,
                -75.5812,
                Instant.parse("2026-09-08T13:00:00Z")
        );

        when(ingestTelemetryUseCase.ingest(any(Telemetry.class)))
                .thenReturn(
                        TelemetryIngestionResult.accepted(telemetry)
                );

        String requestBody = """
                {
                    "vehicleId": "VH-001",
                    "lat": 6.2442,
                    "lng": -75.5812,
                    "timestamp": "2026-09-08T13:00:00Z"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/telemetry")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vehicleId").value("VH-001"))
                .andExpect(jsonPath("$.lat").value(6.2442))
                .andExpect(jsonPath("$.lng").value(-75.5812))
                .andExpect(
                        jsonPath("$.timestamp")
                                .value("2026-09-08T13:00:00Z")
                );
    }

    @Test
    void shouldRejectInvalidLatitude() throws Exception {

        String requestBody = """
                {
                    "vehicleId": "VH-001",
                    "lat": 100.0,
                    "lng": -75.5812,
                    "timestamp": "2026-09-08T13:00:00Z"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/telemetry")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value("VALIDATION_ERROR")
                );
    }

    @Test
    void shouldRejectMissingVehicleId() throws Exception {

        String requestBody = """
                {
                    "lat": 6.2442,
                    "lng": -75.5812,
                    "timestamp": "2026-09-08T13:00:00Z"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/telemetry")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value("VALIDATION_ERROR")
                );
    }

    @Test
    void shouldRejectMalformedJson() throws Exception {

        String requestBody = """
                {
                    "vehicleId": "VH-001",
                    "lat": 6.2442,
                    "lng":
                """;

        mockMvc.perform(
                        post("/api/v1/telemetry")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value("MALFORMED_REQUEST")
                );
    }

    @Test
    void shouldReturnOkWhenTelemetryIsDuplicate() throws Exception {

        Telemetry telemetry = Telemetry.create(
                "VH-001",
                6.2442,
                -75.5812,
                Instant.parse("2026-09-08T13:00:00Z")
        );

        when(ingestTelemetryUseCase.ingest(any(Telemetry.class)))
                .thenReturn(
                        TelemetryIngestionResult.duplicated(telemetry)
                );

        String requestBody = """
            {
                "vehicleId": "VH-001",
                "lat": 6.2442,
                "lng": -75.5812,
                "timestamp": "2026-09-08T13:00:00Z"
            }
            """;

        mockMvc.perform(
                        post("/api/v1/telemetry")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicleId").value("VH-001"))
                .andExpect(jsonPath("$.lat").value(6.2442))
                .andExpect(jsonPath("$.lng").value(-75.5812));
    }
}