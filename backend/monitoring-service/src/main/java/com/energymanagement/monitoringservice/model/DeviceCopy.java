package com.energymanagement.monitoringservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

// Local copy of the device data needed for monitoring, kept in sync through events from the Device Service
@Entity
@Table(name = "device_copy")
public class DeviceCopy {

    @Id
    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "device_name", length = 150)
    private String deviceName;

    // Maximum hourly consumption, in kWh
    @Column(name = "max_consumption", precision = 10, scale = 2)
    private BigDecimal maxConsumption;

    // Owner of the device; null while the device is not assigned
    @Column(name = "user_id")
    private Long userId;

    public DeviceCopy() {
    }

    public DeviceCopy(Long deviceId) {
        this.deviceId = deviceId;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
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