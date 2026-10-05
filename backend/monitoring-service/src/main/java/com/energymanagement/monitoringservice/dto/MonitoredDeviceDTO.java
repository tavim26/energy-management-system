package com.energymanagement.monitoringservice.dto;

import com.energymanagement.monitoringservice.model.DeviceCopy;

import java.math.BigDecimal;

public record MonitoredDeviceDTO(Long deviceId, String deviceName, BigDecimal maxConsumption, Long userId) {

    public static MonitoredDeviceDTO from(DeviceCopy device) {
        return new MonitoredDeviceDTO(
                device.getDeviceId(),
                device.getDeviceName(),
                device.getMaxConsumption(),
                device.getUserId()
        );
    }
}