package com.energymanagement.monitoringservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class DeviceMessage
{

    private LocalDateTime timestamp;

    @JsonProperty("device_id")
    private Long deviceId;

    @JsonProperty("measurement_value")
    private Double measurementValue;

    public DeviceMessage() {
    }

    public DeviceMessage(LocalDateTime timestamp, Long deviceId, Double measurementValue)
    {
        this.timestamp = timestamp;
        this.deviceId = deviceId;
        this.measurementValue = measurementValue;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public Double getMeasurementValue() {
        return measurementValue;
    }

    public void setMeasurementValue(Double measurementValue) {
        this.measurementValue = measurementValue;
    }

    @Override
    public String toString() {
        return "DeviceMessage{" +
                "timestamp=" + timestamp +
                ", deviceId=" + deviceId +
                ", measurementValue=" + measurementValue +
                '}';
    }
}