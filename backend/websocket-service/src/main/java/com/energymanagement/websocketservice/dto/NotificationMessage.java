package com.energymanagement.websocketservice.dto;

// Payload pushed to the browser on /topic/notifications/{userId}
public record NotificationMessage(
        String type,
        Long deviceId,
        String deviceName,
        Double consumption,
        Double limit,
        String timestamp
) {
}