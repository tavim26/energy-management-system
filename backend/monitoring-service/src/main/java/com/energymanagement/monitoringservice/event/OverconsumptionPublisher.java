package com.energymanagement.monitoringservice.event;

import com.energymanagement.monitoringservice.model.DeviceCopy;
import com.energymanagement.monitoringservice.model.HourlyConsumption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.HashMap;
import java.util.Map;

// Sends overconsumption alerts to the WebSocket Service, through the notifications queue
@Component
public class OverconsumptionPublisher {

    private static final Logger log = LoggerFactory.getLogger(OverconsumptionPublisher.class);

    private final RabbitTemplate syncRabbitTemplate;

    @Value("${rabbitmq.queue.notifications}")
    private String notificationsQueue;

    public OverconsumptionPublisher(@Qualifier("syncRabbitTemplate") RabbitTemplate syncRabbitTemplate) {
        this.syncRabbitTemplate = syncRabbitTemplate;
    }

    public void publish(DeviceCopy device, HourlyConsumption consumption) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "OVERCONSUMPTION");
        notification.put("deviceId", device.getDeviceId());
        notification.put("deviceName", device.getDeviceName());
        notification.put("userId", device.getUserId());
        notification.put("consumption", consumption.getTotalKwh());
        notification.put("limit", device.getMaxConsumption());
        notification.put("timestamp", consumption.getHourTimestamp().toString());

        // The alert is sent only after the transaction commits, so it always matches the saved data
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send(notification);
                }
            });
        } else {
            send(notification);
        }
    }

    private void send(Map<String, Object> notification) {
        try {
            // Default exchange: the routing key is the queue name
            syncRabbitTemplate.convertAndSend(notificationsQueue, notification);
            log.info("Overconsumption alert sent for device {} (user {})",
                    notification.get("deviceId"), notification.get("userId"));
        } catch (AmqpException e) {
            log.error("Failed to send overconsumption alert for device {}", notification.get("deviceId"), e);
        }
    }
}