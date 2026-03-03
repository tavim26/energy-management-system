package com.energymanagement.usermanagement.dto;

public class UserDTO {

    private Long id;

    //vin din auth_db prin API call
    private String username;
    private String role;

    //din user_db
    private String fullName;
    private String address;

    public UserDTO() {
    }

    public UserDTO(Long id, String username, String role, String fullName, String address) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.fullName = fullName;
        this.address = address;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}