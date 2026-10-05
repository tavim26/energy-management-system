package com.energymanagement.devicemanagement.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Local copy of the CLIENT users, kept in sync through USER_CREATED / USER_DELETED events,
// so devices can be assigned without calling the User Service
@Entity
@Table(name = "users_copy")
public class UserCopy {

    @Id
    @Column(name = "user_id")
    private Long userId;

    public UserCopy() {
    }

    public UserCopy(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }
}