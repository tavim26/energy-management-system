package com.energymanagement.monitoringservice.controller;

import com.energymanagement.monitoringservice.model.DeviceCopy;
import com.energymanagement.monitoringservice.model.HourlyConsumption;
import com.energymanagement.monitoringservice.repository.DeviceCopyRepository;
import com.energymanagement.monitoringservice.repository.HourlyConsumptionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/monitoring")
@CrossOrigin(origins = "*")
@Tag(name = "Monitoring", description = "Endpoints pentru monitorizarea consumului energetic")
public class MonitoringController
{

    private final HourlyConsumptionRepository repository;
    private final DeviceCopyRepository deviceCopyRepository;

    public MonitoringController(HourlyConsumptionRepository repository, DeviceCopyRepository deviceCopyRepository)
    {
        this.repository = repository;
        this.deviceCopyRepository = deviceCopyRepository;
    }

    @Operation(summary = "Afiseaza toate device-urile sincronizate in Monitoring Service",
            description = "Returneaza lista completa de device ID-uri din tabelul device_copy")
    @GetMapping("/devices")
    public ResponseEntity<Map<String, Object>> getAllDevices()
    {
        List<DeviceCopy> devices = deviceCopyRepository.findAll();

        List<Long> deviceIds = devices.stream()
                .map(DeviceCopy::getDeviceId)
                .sorted()
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("totalDevices", devices.size());
        response.put("deviceIds", deviceIds);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Intoarce datele brute din baza de date pentru un device si o zi specificata",
            description = "Afiseaza toate inregistrarile orare stocate, sortate crescator dupa ora")
    @GetMapping("/device/{deviceId}/raw")
    public ResponseEntity<Map<String, Object>> getDeviceRawData(
            @Parameter(description = "ID-ul device-ului", example = "1")
            @PathVariable Long deviceId,
            @Parameter(description = "Data pentru care se solicita datele (format: YYYY-MM-DD)", example = "2025-11-21")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        List<HourlyConsumption> records = repository.findByDeviceIdAndHourTimestampBetween(
                deviceId,
                startOfDay,
                endOfDay
        );

        List<Map<String, Object>> dataList = new ArrayList<>();
        for (HourlyConsumption record : records) {
            Map<String, Object> item = new HashMap<>();
            item.put("hour", record.getHourTimestamp().getHour());
            item.put("timestamp", record.getHourTimestamp().toString());
            item.put("totalKwh", record.getTotalKwh());
            item.put("lastUpdated", record.getUpdatedAt().toString());
            dataList.add(item);
        }

        dataList.sort(Comparator.comparingInt(m -> (Integer) m.get("hour")));

        Map<String, Object> response = new HashMap<>();
        response.put("deviceId", deviceId);
        response.put("date", date.toString());
        response.put("totalRecords", records.size());
        response.put("data", dataList);
        response.put("message", "Date brute din baza de date, sortate crescator dupa ora");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Returneaza consumul energetic agregat pe 24 de ore pentru o zi specificata",
            description = "Afiseaza consumul pentru fiecare ora (0-23), completand cu 0.0 kWh pentru orele fara date")
    @GetMapping("/device/{deviceId}/history")
    public ResponseEntity<Map<String, Object>> getDeviceHistory(
            @Parameter(description = "ID-ul device-ului", example = "1")
            @PathVariable Long deviceId,
            @Parameter(description = "Data pentru care se solicita istoricul (format: YYYY-MM-DD)", example = "2025-11-21")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        List<HourlyConsumption> records = repository.findByDeviceIdAndHourTimestampBetween(
                deviceId,
                startOfDay,
                endOfDay
        );

        Map<Integer, Double> hourlyData = new LinkedHashMap<>();
        for (int hour = 0; hour < 24; hour++)
        {
            hourlyData.put(hour, 0.0);
        }

        for (HourlyConsumption record : records)
        {
            int hour = record.getHourTimestamp().getHour();
            hourlyData.put(hour, record.getTotalKwh().doubleValue());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("deviceId", deviceId);
        response.put("date", date.toString());
        response.put("hourlyConsumption", hourlyData);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Afiseaza statusul general al unui device in sistem",
            description = "Returneaza numarul total de inregistrari si intervalul temporal (prima si ultima inregistrare)")
    @GetMapping("/device/{deviceId}/status")
    public ResponseEntity<Map<String, Object>> getDeviceStatus(
            @Parameter(description = "ID-ul device-ului", example = "1")
            @PathVariable Long deviceId
    ) {
        List<HourlyConsumption> allRecords = repository.findAll()
                .stream()
                .filter(r -> r.getDeviceId().equals(deviceId))
                .sorted(Comparator.comparing(HourlyConsumption::getHourTimestamp))
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("deviceId", deviceId);
        response.put("totalRecords", allRecords.size());

        if (!allRecords.isEmpty()) {
            response.put("firstRecord", allRecords.get(0).getHourTimestamp().toString());
            response.put("lastRecord", allRecords.get(allRecords.size() - 1).getHourTimestamp().toString());
            response.put("status", "active");
        } else {
            response.put("status", "no data");
        }

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Returneaza ultimele N inregistrari pentru monitorizare in timp real",
            description = "Afiseaza cele mai recente masuratori ale device-ului, sortate descrescator dupa timestamp")
    @GetMapping("/device/{deviceId}/latest")
    public ResponseEntity<Map<String, Object>> getLatestRecords(
            @Parameter(description = "ID-ul device-ului", example = "1")
            @PathVariable Long deviceId,
            @Parameter(description = "Numarul de inregistrari de returnat", example = "10")
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<HourlyConsumption> allRecords = repository.findAll()
                .stream()
                .filter(r -> r.getDeviceId().equals(deviceId))
                .sorted(Comparator.comparing(HourlyConsumption::getHourTimestamp).reversed())
                .limit(limit)
                .toList();

        List<Map<String, Object>> records = new ArrayList<>();
        for (HourlyConsumption record : allRecords) {
            Map<String, Object> item = new HashMap<>();
            item.put("timestamp", record.getHourTimestamp().toString());
            item.put("totalKwh", record.getTotalKwh());
            item.put("updatedAt", record.getUpdatedAt().toString());
            records.add(item);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("deviceId", deviceId);
        response.put("limit", limit);
        response.put("records", records);

        return ResponseEntity.ok(response);
    }
}