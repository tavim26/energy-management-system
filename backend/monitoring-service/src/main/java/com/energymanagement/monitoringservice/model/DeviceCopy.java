package com.energymanagement.monitoringservice.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "device_copy")
public class DeviceCopy {

    @Id
    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "max_consumption")
    private BigDecimal maxConsumption;

    @Column(name = "user_id")
    private Long userId;

    public DeviceCopy() {
    }

    public DeviceCopy(Long deviceId, BigDecimal maxConsumption, Long userId) {
        this.deviceId = deviceId;
        this.maxConsumption = maxConsumption;
        this.userId = userId;
    }

    // Getters & Setters
    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
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