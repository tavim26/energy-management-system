package com.energymanagement.authorizationservice.dto;

public class AdminCreateUserDTO extends RegisterRequestDTO {

    private String role;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}