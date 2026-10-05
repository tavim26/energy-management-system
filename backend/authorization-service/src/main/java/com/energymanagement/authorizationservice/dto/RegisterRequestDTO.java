package com.energymanagement.authorizationservice.dto;

public class RegisterRequestDTO {

    private String username;
    private String password;
    private String fullName;
    private String address;

    public RegisterRequestDTO() {
    }

    public RegisterRequestDTO(String username, String password, String fullName, String address) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.address = address;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
}