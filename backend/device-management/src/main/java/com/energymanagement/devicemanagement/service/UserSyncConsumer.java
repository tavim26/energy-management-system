package com.energymanagement.devicemanagement.service;

import com.energymanagement.devicemanagement.event.SyncEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UserSyncConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserSyncConsumer.class);

    private final DeviceService deviceService;

    public UserSyncConsumer(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @RabbitListener(queues = "sync-queue-device", containerFactory = "syncRabbitListenerContainerFactory")
    public void consumeSyncEvent(Map<String, Object> message) {
        Object eventType = message.get("eventType");

        try {
            if (SyncEventType.USER_CREATED.equals(eventType)) {
                // Admin accounts are not tracked, so devices can only be assigned to clients
                if ("CLIENT".equals(message.get("role"))) {
                    Long userId = readUserId(message);
                    deviceService.registerUser(userId);
                    log.info("User {} added to users_copy", userId);
                }
            } else if (SyncEventType.USER_DELETED.equals(eventType)) {
                Long userId = readUserId(message);
                deviceService.removeUser(userId);
                log.info("User {} removed and their devices unassigned", userId);
            }
        } catch (Exception e) {
            // The message is dropped instead of being redelivered forever
            log.error("Failed to process sync event {}", eventType, e);
        }
    }

    private Long readUserId(Map<String, Object> message) {
        return ((Number) message.get("userId")).longValue();
    }
}