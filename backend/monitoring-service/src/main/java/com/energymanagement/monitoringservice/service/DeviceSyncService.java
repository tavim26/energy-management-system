package com.energymanagement.monitoringservice.service;

import com.energymanagement.monitoringservice.model.DeviceCopy;
import com.energymanagement.monitoringservice.repository.DeviceCopyRepository;
import com.energymanagement.monitoringservice.repository.HourlyConsumptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

// Keeps the device_copy table in sync with the Device Service
@Service
public class DeviceSyncService {

    private final DeviceCopyRepository deviceCopyRepository;
    private final HourlyConsumptionRepository consumptionRepository;

    public DeviceSyncService(
            DeviceCopyRepository deviceCopyRepository,
            HourlyConsumptionRepository consumptionRepository
    ) {
        this.deviceCopyRepository = deviceCopyRepository;
        this.consumptionRepository = consumptionRepository;
    }

    // Used for both DEVICE_CREATED and DEVICE_UPDATED; the owner is not changed here
    @Transactional
    public void saveDeviceData(Long deviceId, String deviceName, BigDecimal maxConsumption) {
        DeviceCopy device = deviceCopyRepository.findById(deviceId).orElseGet(() -> new DeviceCopy(deviceId));
        device.setDeviceName(deviceName);
        device.setMaxConsumption(maxConsumption);
        deviceCopyRepository.save(device);
    }

    // userId is null when the device is unassigned
    @Transactional
    public void setOwner(Long deviceId, Long userId) {
        DeviceCopy device = deviceCopyRepository.findById(deviceId).orElseGet(() -> new DeviceCopy(deviceId));
        device.setUserId(userId);
        deviceCopyRepository.save(device);
    }

    @Transactional
    public void deleteDevice(Long deviceId) {
        consumptionRepository.deleteByDeviceId(deviceId);
        deviceCopyRepository.deleteById(deviceId);
    }
}