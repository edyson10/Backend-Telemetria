package com.movilidad.backendtelemetria.infrastructure.adapter.input.rest.controller;

import com.movilidad.backendtelemetria.application.port.input.GetRecentAlertsUseCase;
import com.movilidad.backendtelemetria.infrastructure.adapter.input.rest.mapper.AlertRestMapper;
import com.movilidad.backendtelemetria.infrastructure.adapter.input.rest.response.AlertResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {

    private final GetRecentAlertsUseCase getRecentAlertsUseCase;
    private final AlertRestMapper alertRestMapper;

    public AlertController(
            GetRecentAlertsUseCase getRecentAlertsUseCase,
            AlertRestMapper alertRestMapper
    ) {
        this.getRecentAlertsUseCase = getRecentAlertsUseCase;
        this.alertRestMapper = alertRestMapper;
    }

    @GetMapping
    public List<AlertResponse> getRecentAlerts(
            @RequestParam(defaultValue = "20") int limit
    ) {

        return getRecentAlertsUseCase
                .getRecentAlerts(limit)
                .stream()
                .map(alertRestMapper::toResponse)
                .toList();
    }
}
