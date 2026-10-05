package com.energymanagement.monitoringservice.controller;

import com.energymanagement.monitoringservice.dto.DailyConsumptionDTO;
import com.energymanagement.monitoringservice.dto.HourlyConsumptionDTO;
import com.energymanagement.monitoringservice.dto.MonitoredDeviceDTO;
import com.energymanagement.monitoringservice.service.MonitoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/monitoring")
@Tag(name = "Monitoring", description = "Hourly energy consumption of the devices")
public class MonitoringController {

    private final MonitoringService monitoringService;

    public MonitoringController(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @Operation(summary = "List the devices known by the monitoring service")
    @GetMapping("/devices")
    public List<MonitoredDeviceDTO> getMonitoredDevices() {
        return monitoringService.getMonitoredDevices();
    }

    @Operation(summary = "Consumption of a device for each hour of a day (0 kWh for hours without data)")
    @GetMapping("/device/{deviceId}/history")
    public DailyConsumptionDTO getDailyConsumption(
            @PathVariable Long deviceId,
            @Parameter(description = "Day, in YYYY-MM-DD format", example = "2026-10-05")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return monitoringService.getDailyConsumption(deviceId, date);
    }

    @Operation(summary = "Most recent hourly records of a device, newest first")
    @GetMapping("/device/{deviceId}/latest")
    public List<HourlyConsumptionDTO> getLatestConsumption(
            @PathVariable Long deviceId,
            @Parameter(description = "Number of records (1-100)", example = "10")
            @RequestParam(defaultValue = "10") int limit
    ) {
        return monitoringService.getLatestConsumption(deviceId, limit);
    }
}