package com.energymanagement.devicemanagement.event;

import com.energymanagement.devicemanagement.model.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.HashMap;
import java.util.Map;

// Publishes device events on the fanout sync exchange, so every subscribed service receives them
@Component
public class DeviceEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(DeviceEventPublisher.class);

    private final RabbitTemplate syncRabbitTemplate;

    @Value("${rabbitmq.exchange.sync}")
    private String syncExchangeName;

    public DeviceEventPublisher(RabbitTemplate syncRabbitTemplate) {
        this.syncRabbitTemplate = syncRabbitTemplate;
    }

    public void deviceCreated(Device device) {
        publish(withDeviceData(SyncEventType.DEVICE_CREATED, device));
    }

    public void deviceUpdated(Device device) {
        publish(withDeviceData(SyncEventType.DEVICE_UPDATED, device));
    }

    public void deviceAssigned(Long deviceId, Long userId) {
        Map<String, Object> event = event(SyncEventType.DEVICE_ASSIGNED, deviceId);
        event.put("userId", userId);
        publish(event);
    }

    public void deviceUnassigned(Long deviceId) {
        publish(event(SyncEventType.DEVICE_UNASSIGNED, deviceId));
    }

    public void deviceDeleted(Long deviceId) {
        publish(event(SyncEventType.DEVICE_DELETED, deviceId));
    }

    private Map<String, Object> event(String eventType, Long deviceId) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", eventType);
        event.put("deviceId", deviceId);
        return event;
    }

    private Map<String, Object> withDeviceData(String eventType, Device device) {
        Map<String, Object> event = event(eventType, device.getId());
        event.put("deviceName", device.getName());
        event.put("maxConsumption", device.getMaxConsumption());
        return event;
    }

    // Inside a transaction, the event is sent only after the commit succeeds,
    // so other services never hear about a change that was rolled back
    private void publish(Map<String, Object> event) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send(event);
                }
            });
        } else {
            send(event);
        }
    }

    private void send(Map<String, Object> event) {
        try {
            syncRabbitTemplate.convertAndSend(syncExchangeName, "", event);
            log.info("Published {} for device {}", event.get("eventType"), event.get("deviceId"));
        } catch (AmqpException e) {
            log.error("Failed to publish {} for device {}", event.get("eventType"), event.get("deviceId"), e);
        }
    }
}