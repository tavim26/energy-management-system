package com.energymanagement.devicemanagement.controller;

import com.energymanagement.devicemanagement.dto.AssignDeviceDTO;
import com.energymanagement.devicemanagement.dto.CreateDeviceDTO;
import com.energymanagement.devicemanagement.dto.DeviceDTO;
import com.energymanagement.devicemanagement.dto.UpdateDeviceDTO;
import com.energymanagement.devicemanagement.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    public List<DeviceDTO> getAllDevices() {
        return deviceService.getAllDevices();
    }

    @GetMapping("/{id}")
    public DeviceDTO getDeviceById(@PathVariable Long id) {
        return deviceService.getDeviceById(id);
    }

    @GetMapping("/user/{userId}")
    public List<DeviceDTO> getDevicesByUserId(@PathVariable Long userId) {
        return deviceService.getDevicesByUserId(userId);
    }

    @PostMapping
    public ResponseEntity<DeviceDTO> createDevice(@Valid @RequestBody CreateDeviceDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deviceService.createDevice(request));
    }

    @PutMapping("/{id}")
    public DeviceDTO updateDevice(@PathVariable Long id, @Valid @RequestBody UpdateDeviceDTO request) {
        return deviceService.updateDevice(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/assign")
    public ResponseEntity<Void> assignDevice(@Valid @RequestBody AssignDeviceDTO request) {
        deviceService.assignDevice(request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/assignment")
    public ResponseEntity<Void> unassignDevice(@PathVariable Long id) {
        deviceService.unassignDevice(id);
        return ResponseEntity.noContent().build();
    }
}