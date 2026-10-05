package com.energymanagement.monitoringservice.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Total energy consumed by one device during one hour (e.g. 14:00 - 15:00)
@Entity
@Table(
        name = "hourly_consumption",
        uniqueConstraints = @UniqueConstraint(name = "unique_device_hour", columnNames = {"device_id", "hour_timestamp"})
)
public class HourlyConsumption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    // Start of the hour, e.g. 2026-10-05T14:00
    @Column(name = "hour_timestamp", nullable = false)
    private LocalDateTime hourTimestamp;

    @Column(name = "total_kwh", nullable = false, precision = 10, scale = 4)
    private BigDecimal totalKwh = BigDecimal.ZERO;

    // Ensures a single overconsumption alert per device per hour
    @Column(name = "alert_sent", nullable = false, columnDefinition = "boolean default false")
    private boolean alertSent;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected HourlyConsumption() {
    }

    public HourlyConsumption(Long deviceId, LocalDateTime hourTimestamp) {
        this.deviceId = deviceId;
        this.hourTimestamp = hourTimestamp;
    }

    public void addKwh(BigDecimal kwh) {
        this.totalKwh = this.totalKwh.add(kwh);
    }

    public void markAlertSent() {
        this.alertSent = true;
    }

    @PrePersist
    @PreUpdate
    void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public LocalDateTime getHourTimestamp() {
        return hourTimestamp;
    }

    public BigDecimal getTotalKwh() {
        return totalKwh;
    }

    public boolean isAlertSent() {
        return alertSent;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}