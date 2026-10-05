package com.energymanagement.monitoringservice.service;

import com.energymanagement.monitoringservice.dto.DeviceMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

// Receives the measurements sent by the device simulator on the data broker
@Component
public class DeviceDataConsumer {

    private static final Logger log = LoggerFactory.getLogger(DeviceDataConsumer.class);

    private final MonitoringService monitoringService;

    public DeviceDataConsumer(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @RabbitListener(queues = "${rabbitmq.queue.data}", containerFactory = "dataRabbitListenerContainerFactory")
    public void consumeDeviceData(DeviceMessage message) {
        try {
            monitoringService.processMeasurement(message);
        } catch (Exception e) {
            // The message is dropped instead of being redelivered forever
            log.error("Failed to process measurement {}", message, e);
        }
    }
}