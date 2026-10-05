package com.energymanagement.devicemanagement.dto;

import jakarta.validation.constraints.NotNull;

public record AssignDeviceDTO(

        @NotNull(message = "User id is required")
        Long userId,

        @NotNull(message = "Device id is required")
        Long deviceId
) {
}