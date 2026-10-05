package com.energymanagement.devicemanagement.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

// Both fields are optional: only the ones sent are changed
public record UpdateDeviceDTO(

        @Size(max = 150, message = "Device name must have at most 150 characters")
        String name,

        @Positive(message = "Max consumption must be greater than 0")
        BigDecimal maxConsumption
) {
}