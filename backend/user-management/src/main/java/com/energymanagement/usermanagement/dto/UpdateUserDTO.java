package com.energymanagement.usermanagement.dto;

public class UpdateUserDTO {

    private String fullName;
    private String address;

    public UpdateUserDTO() {
    }

    public UpdateUserDTO(String fullName, String address) {
        this.fullName = fullName;
        this.address = address;
    }

    public String getFullName() {
        return fullName;
    }

    public String getAddress() {
        return address;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}