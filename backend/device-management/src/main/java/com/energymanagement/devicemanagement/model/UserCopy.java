package com.energymanagement.devicemanagement.model;

import jakarta.persistence.*;


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

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}