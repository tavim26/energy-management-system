package com.energymanagement.authorizationservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 100, message = "Username must have between 3 and 100 characters")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 100, message = "Password must have between 6 and 100 characters")
        String password,

        @Size(max = 200, message = "Full name must have at most 200 characters")
        String fullName,

        @Size(max = 300, message = "Address must have at most 300 characters")
        String address
) {
}