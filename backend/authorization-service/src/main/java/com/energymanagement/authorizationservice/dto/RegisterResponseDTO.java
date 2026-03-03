package com.energymanagement.authorizationservice.dto;

public class RegisterResponseDTO {

    private Long userId;
    private String username;
    private String role;
    private String fullName;
    private String address;
    private String message;

    public RegisterResponseDTO() {
    }

    public RegisterResponseDTO(Long userId, String username, String role, String fullName, String address, String message) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.fullName = fullName;
        this.address = address;
        this.message = message;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}