package com.energymanagement.usermanagement.dto;

import jakarta.validation.constraints.Size;

public record CreateProfileDTO(

        @Size(max = 200, message = "Full name must have at most 200 characters")
        String fullName,

        @Size(max = 300, message = "Address must have at most 300 characters")
        String address
) {
}