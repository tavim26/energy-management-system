package com.energymanagement.authorizationservice.dto;

public record RegisterResponseDTO(Long userId, String username, String role, String fullName, String address) {
}