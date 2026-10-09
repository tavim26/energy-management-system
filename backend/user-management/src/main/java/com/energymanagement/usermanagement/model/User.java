package com.energymanagement.usermanagement.model;

import jakarta.persistence.*;

// Profile data of a user. Username, password and role are stored by the Authorization Service.
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", length = 200)
    private String fullName;

    @Column(length = 300)
    private String address;

    protected User() {
    }

    public User(String fullName, String address) {
        this.fullName = fullName;
        this.address = address;
    }

    public Long getId() {
        return id;
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