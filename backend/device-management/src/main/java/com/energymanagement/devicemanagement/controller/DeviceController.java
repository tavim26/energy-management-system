package com.energymanagement.devicemanagement.controller;

import com.energymanagement.devicemanagement.dto.*;
import com.energymanagement.devicemanagement.service.DeviceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/devices")

public class DeviceController
{

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService)
    {
        this.deviceService = deviceService;
    }

    // POST /api/devices
    @PostMapping
    public ResponseEntity<DeviceDTO> createDevice(@RequestBody CreateDeviceDTO createDeviceDTO)
    {
        try {

            DeviceDTO createdDevice = deviceService.createDevice(createDeviceDTO);

            return new ResponseEntity<>(createdDevice, HttpStatus.CREATED);

        } catch (RuntimeException e)
        {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    // GET /api/devices
    @GetMapping
    public ResponseEntity<List<DeviceDTO>> getAllDevices()
    {
        List<DeviceDTO> devices = deviceService.getAllDevices();

        return ResponseEntity.ok(devices);
    }

    // GET /api/devices/{id}
    @GetMapping("/{id}")
    public ResponseEntity<DeviceDTO> getDeviceById(@PathVariable Long id)
    {
        Optional<DeviceDTO> deviceOptional = deviceService.getDeviceById(id);

        if (deviceOptional.isPresent())
        {
            return ResponseEntity.ok(deviceOptional.get());
        }
        else
        {
            return ResponseEntity.notFound().build();
        }
    }

    // PUT /api/devices/{id}
    @PutMapping("/{id}")
    public ResponseEntity<DeviceDTO> updateDevice(@PathVariable Long id, @RequestBody UpdateDeviceDTO updateDeviceDTO)
    {
        try {
            DeviceDTO updatedDevice = deviceService.updateDevice(id, updateDeviceDTO);

            return ResponseEntity.ok(updatedDevice);

        } catch (RuntimeException e)
        {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /api/devices/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id)
    {
        try {
            deviceService.deleteDevice(id);

            return ResponseEntity.noContent().build();

        } catch (RuntimeException e)
        {
            return ResponseEntity.notFound().build();
        }
    }

    // POST /api/devices/assign
    @PostMapping("/assign")
    public ResponseEntity<Void> assignDeviceToUser(@RequestBody AssignDeviceDTO assignDeviceDTO)
    {
        try {

            deviceService.assignDeviceToUser(assignDeviceDTO);
            return ResponseEntity.status(HttpStatus.CREATED).build();

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }


    @DeleteMapping("/user/{userId}/devices")
    public ResponseEntity<Void> deleteAllUserDevices(@PathVariable Long userId)
    {
        try {

            deviceService.deleteAllDevicesForUser(userId);
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    // GET /api/devices/user/{userId}
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DeviceDTO>> getDevicesByUserId(@PathVariable Long userId)
    {
        List<DeviceDTO> devices = deviceService.getDevicesByUserId(userId);

        return ResponseEntity.ok(devices);
    }




}