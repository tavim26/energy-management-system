package com.energymanagement.authorizationservice.dto;

public record LoginResponseDTO(String token, Long userId, String username, String role) {
}