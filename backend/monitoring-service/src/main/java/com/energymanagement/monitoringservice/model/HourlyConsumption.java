package com.energymanagement.monitoringservice.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "hourly_consumption",
        uniqueConstraints = @UniqueConstraint(
                name = "unique_device_hour",
                columnNames = {"device_id", "hour_timestamp"}
        )
)
public class HourlyConsumption
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    @Column(name = "hour_timestamp", nullable = false)
    private LocalDateTime hourTimestamp;

    @Column(name = "total_kwh", nullable = false, precision = 10, scale = 4)
    private BigDecimal totalKwh;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public HourlyConsumption() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public HourlyConsumption(Long deviceId, LocalDateTime hourTimestamp, BigDecimal totalKwh) {
        this.deviceId = deviceId;
        this.hourTimestamp = hourTimestamp;
        this.totalKwh = totalKwh;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public LocalDateTime getHourTimestamp() {
        return hourTimestamp;
    }

    public void setHourTimestamp(LocalDateTime hourTimestamp) {
        this.hourTimestamp = hourTimestamp;
    }

    public BigDecimal getTotalKwh() {
        return totalKwh;
    }

    public void setTotalKwh(BigDecimal totalKwh) {
        this.totalKwh = totalKwh;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}