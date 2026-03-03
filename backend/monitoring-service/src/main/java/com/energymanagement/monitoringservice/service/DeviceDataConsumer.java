package com.energymanagement.monitoringservice.service;

import com.energymanagement.monitoringservice.dto.DeviceMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class DeviceDataConsumer
{

    private final MonitoringService monitoringService;

    public DeviceDataConsumer(MonitoringService monitoringService)
    {
        this.monitoringService = monitoringService;
    }

    // Consuma de pe Data Broker
    @RabbitListener(
            queues = "${rabbitmq.queue.data}",
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void consumeDeviceData(DeviceMessage message)
    {

        System.out.println("Received device data from Data Broker: " + message);

        try {

            monitoringService.processDeviceMessage(message);

        }
        catch (Exception e)
        {
            System.err.println("Error processing message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}