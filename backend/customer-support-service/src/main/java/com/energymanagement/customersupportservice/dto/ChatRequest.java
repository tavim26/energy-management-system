package com.energymanagement.customersupportservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(

        @NotBlank(message = "Message cannot be empty")
        @Size(max = 1000, message = "Message must have at most 1000 characters")
        String message
) {
}