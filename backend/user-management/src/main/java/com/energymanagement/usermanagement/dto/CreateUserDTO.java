package com.energymanagement.usermanagement.dto;

public class CreateUserDTO {

    //trimise catre Auth Service
    private String username;
    private String password;
    private String role;

    //salvate in user_db
    private String fullName;
    private String address;

    public CreateUserDTO() {
    }

    public CreateUserDTO(String username, String password, String role, String fullName, String address) {
        this.username = username;
        this.password = password;
        this.role = role;
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
}