package com.energymanagement.devicemanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateDeviceDTO(

        @NotBlank(message = "Device name is required")
        @Size(max = 150, message = "Device name must have at most 150 characters")
        String name,

        @NotNull(message = "Max consumption is required")
        @Positive(message = "Max consumption must be greater than 0")
        BigDecimal maxConsumption
) {
}