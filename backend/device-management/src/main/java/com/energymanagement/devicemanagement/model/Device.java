package com.energymanagement.devicemanagement.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "devices")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "max_consumption", nullable = false, precision = 10, scale = 2)
    private BigDecimal maxConsumption;

    @Column(name = "user_id")
    private Long userId;

    public Device() {
    }

    public Device(Long id, String name, BigDecimal maxConsumption, Long userId) {
        this.id = id;
        this.name = name;
        this.maxConsumption = maxConsumption;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getMaxConsumption() {
        return maxConsumption;
    }

    public void setMaxConsumption(BigDecimal maxConsumption) {
        this.maxConsumption = maxConsumption;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}