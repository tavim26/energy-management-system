package com.energymanagement.usermanagement.dto;

import com.energymanagement.usermanagement.model.User;

// Combines the profile stored here with the account data from the Authorization Service
public record UserDTO(Long id, String username, String role, String fullName, String address) {

    // credentials can be null if the account no longer exists in the Authorization Service
    public static UserDTO from(User user, CredentialDTO credentials) {
        return new UserDTO(
                user.getId(),
                credentials == null ? null : credentials.username(),
                credentials == null ? null : credentials.role(),
                user.getFullName(),
                user.getAddress()
        );
    }
}