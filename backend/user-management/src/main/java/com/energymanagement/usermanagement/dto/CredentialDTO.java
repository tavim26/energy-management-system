package com.energymanagement.usermanagement.dto;

// Account data returned by the Authorization Service
public record CredentialDTO(Long userId, String username, String role) {
}