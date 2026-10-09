package com.energymanagement.authorizationservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Login data of a user. The id is the same as the user's id in the User Service.
@Entity
@Table(name = "credentials")
public class Credential {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    // BCrypt hash; the plain password is never stored
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 20)
    private String role;

    protected Credential() {
    }

    public Credential(Long id, String username, String passwordHash, String role) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRole() {
        return role;
    }
}