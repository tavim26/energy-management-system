package com.energymanagement.monitoringservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

import static com.energymanagement.monitoringservice.event.SyncEventType.*;

@Component
public class SyncEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(SyncEventConsumer.class);

    private final DeviceSyncService deviceSyncService;

    public SyncEventConsumer(DeviceSyncService deviceSyncService) {
        this.deviceSyncService = deviceSyncService;
    }

    @RabbitListener(queues = "${rabbitmq.queue.sync}", containerFactory = "syncRabbitListenerContainerFactory")
    public void consumeSyncEvent(Map<String, Object> message) {
        Object eventType = message.get("eventType");

        try {
            switch (String.valueOf(eventType)) {
                case DEVICE_CREATED, DEVICE_UPDATED -> deviceSyncService.saveDeviceData(
                        readLong(message, "deviceId"),
                        (String) message.get("deviceName"),
                        readDecimal(message, "maxConsumption"));
                case DEVICE_ASSIGNED -> deviceSyncService.setOwner(
                        readLong(message, "deviceId"),
                        readLong(message, "userId"));
                case DEVICE_UNASSIGNED -> deviceSyncService.setOwner(readLong(message, "deviceId"), null);
                case DEVICE_DELETED -> deviceSyncService.deleteDevice(readLong(message, "deviceId"));
                // User events are also broadcast on this exchange, but are not needed here
                default -> {
                    return;
                }
            }

            log.info("Processed {} for device {}", eventType, message.get("deviceId"));

        } catch (Exception e) {
            // The message is dropped instead of being redelivered forever
            log.error("Failed to process sync event {}", eventType, e);
        }
    }

    private Long readLong(Map<String, Object> message, String key) {
        return ((Number) message.get(key)).longValue();
    }

    private BigDecimal readDecimal(Map<String, Object> message, String key) {
        Object value = message.get(key);
        return value == null ? null : new BigDecimal(value.toString());
    }
}