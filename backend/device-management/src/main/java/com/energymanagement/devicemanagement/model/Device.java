package com.energymanagement.devicemanagement.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "devices")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    // Maximum hourly consumption, in kWh
    @Column(name = "max_consumption", nullable = false, precision = 10, scale = 2)
    private BigDecimal maxConsumption;

    // Owner of the device; null while the device is not assigned
    @Column(name = "user_id")
    private Long userId;

    public Device() {
    }

    public Device(String name, BigDecimal maxConsumption) {
        this.name = name;
        this.maxConsumption = maxConsumption;
    }

    public Long getId() {
        return id;
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