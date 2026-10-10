package com.energymanagement.simulator;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

// Sent as JSON: {"timestamp": "2026-10-10T14:20:00", "device_id": 1, "measurement_value": 1532.45}
// measurement_value is the average power over the last 10 minutes, in watts
public record DeviceMessage(

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime timestamp,

        @JsonProperty("device_id")
        long deviceId,

        @JsonProperty("measurement_value")
        double measurementValue
) {
}