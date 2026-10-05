package com.energymanagement.devicemanagement.dto;

import com.energymanagement.devicemanagement.model.Device;

import java.math.BigDecimal;

public record DeviceDTO(Long id, String name, BigDecimal maxConsumption, Long userId) {

    public static DeviceDTO from(Device device) {
        return new DeviceDTO(device.getId(), device.getName(), device.getMaxConsumption(), device.getUserId());
    }
}