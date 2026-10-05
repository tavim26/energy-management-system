package com.energymanagement.websocketservice.service;

import com.energymanagement.websocketservice.dto.NotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

// Forwards the alerts received from the Monitoring Service to the browser of the device owner
@Component
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationConsumer(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @RabbitListener(queues = "${rabbitmq.queue.notifications}")
    public void consumeNotification(Map<String, Object> message) {
        try {
            Long userId = readNumber(message, "userId").longValue();

            NotificationMessage notification = new NotificationMessage(
                    (String) message.get("type"),
                    readNumber(message, "deviceId").longValue(),
                    (String) message.get("deviceName"),
                    readNumber(message, "consumption").doubleValue(),
                    readNumber(message, "limit").doubleValue(),
                    (String) message.get("timestamp")
            );

            messagingTemplate.convertAndSend("/topic/notifications/" + userId, notification);
            log.info("Notification for device {} sent to user {}", notification.deviceId(), userId);

        } catch (Exception e) {
            // The message is dropped instead of being redelivered forever
            log.error("Failed to forward notification {}", message, e);
        }
    }

    private Number readNumber(Map<String, Object> message, String key) {
        Object value = message.get(key);

        if (!(value instanceof Number number)) {
            throw new IllegalArgumentException("Missing or invalid field '" + key + "'");
        }

        return number;
    }
}