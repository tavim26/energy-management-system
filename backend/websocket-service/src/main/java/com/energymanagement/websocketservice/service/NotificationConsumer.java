package com.energymanagement.websocketservice.service;

import com.energymanagement.websocketservice.dto.NotificationMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NotificationConsumer
{

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationConsumer(SimpMessagingTemplate messagingTemplate)
    {
        this.messagingTemplate = messagingTemplate;
    }

    @RabbitListener(queues = "${rabbitmq.queue.notifications}")
    public void consumeNotification(Map<String, Object> message)
    {
        try {
            System.out.println("Received notification from RabbitMQ: " + message);

            String type = (String) message.get("type");
            Long deviceId = ((Number) message.get("deviceId")).longValue();
            Long userId = ((Number) message.get("userId")).longValue();  // EXTRACT userId
            Double consumption = ((Number) message.get("consumption")).doubleValue();
            Double limit = ((Number) message.get("limit")).doubleValue();

            NotificationMessage notification = new NotificationMessage();
            notification.setType(type);
            notification.setDeviceId(deviceId);
            notification.setConsumption(consumption);
            notification.setLimit(limit);

            // Trimite la topic specific user-ului
            String userTopic = "/topic/notifications/" + userId;
            messagingTemplate.convertAndSend(userTopic, notification);

            System.out.println("Notification sent to user " + userId + " at topic: " + userTopic);

        } catch (Exception e) {
            System.err.println("Error processing notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
}