package com.energymanagement.monitoringservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

// Measurement sent by the device simulator: average power (in watts) over the last 10 minutes
public record DeviceMessage(

        LocalDateTime timestamp,

        @JsonProperty("device_id")
        Long deviceId,

        @JsonProperty("measurement_value")
        Double measurementValue
) {
}