package com.energymanagement.websocketservice.dto;

import java.time.LocalDateTime;

public class NotificationMessage {

    private String type;
    private Long deviceId;
    private Double consumption;
    private Double limit;
    private LocalDateTime timestamp;

    public NotificationMessage() {
    }

    public NotificationMessage(String type, Long deviceId, Double consumption, Double limit, LocalDateTime timestamp) {
        this.type = type;
        this.deviceId = deviceId;
        this.consumption = consumption;
        this.limit = limit;
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public Double getConsumption() {
        return consumption;
    }

    public void setConsumption(Double consumption) {
        this.consumption = consumption;
    }

    public Double getLimit() {
        return limit;
    }

    public void setLimit(Double limit) {
        this.limit = limit;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "NotificationMessage{" +
                "type='" + type + '\'' +
                ", deviceId=" + deviceId +
                ", consumption=" + consumption +
                ", limit=" + limit +
                ", timestamp=" + timestamp +
                '}';
    }
}