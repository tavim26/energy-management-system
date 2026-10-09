package com.energymanagement.authorizationservice.dto;

import com.energymanagement.authorizationservice.model.Credential;

// Account data shared with the User Service; never contains the password hash
public record CredentialDTO(Long userId, String username, String role) {

    public static CredentialDTO from(Credential credential) {
        return new CredentialDTO(credential.getId(), credential.getUsername(), credential.getRole());
    }
}