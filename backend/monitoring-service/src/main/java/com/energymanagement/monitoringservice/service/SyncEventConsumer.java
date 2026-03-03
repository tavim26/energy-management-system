package com.energymanagement.monitoringservice.service;

import com.energymanagement.monitoringservice.model.DeviceCopy;
import com.energymanagement.monitoringservice.repository.DeviceCopyRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class SyncEventConsumer
{

    private final DeviceCopyRepository deviceCopyRepository;

    public SyncEventConsumer(DeviceCopyRepository deviceCopyRepository)
    {
        this.deviceCopyRepository = deviceCopyRepository;
    }

    @RabbitListener(
            queues = "${rabbitmq.queue.sync}",
            containerFactory = "syncRabbitListenerContainerFactory"
    )
    public void consumeSyncEvent(Map<String, Object> message)
    {
        try {
            String eventType = (String) message.get("eventType");
            System.out.println("Received sync event in Monitoring Service: " + message);

            if ("DEVICE_CREATED".equals(eventType))
            {
                Long deviceId = ((Number) message.get("deviceId")).longValue();

                BigDecimal maxConsumption = null;
                if (message.get("maxConsumption") != null)
                {
                    Number maxConsValue = (Number) message.get("maxConsumption");
                    maxConsumption = BigDecimal.valueOf(maxConsValue.doubleValue());
                }

                // userId este NULL initial
                DeviceCopy deviceCopy = new DeviceCopy(deviceId, maxConsumption, null);
                deviceCopyRepository.save(deviceCopy);

                System.out.println("Device ID " + deviceId + " saved in device_copy (userId=NULL)");
            }
            else if ("DEVICE_ASSIGNED".equals(eventType))
            {
                Long deviceId = ((Number) message.get("deviceId")).longValue();
                Long userId = ((Number) message.get("userId")).longValue();

                DeviceCopy deviceCopy = deviceCopyRepository.findById(deviceId).orElse(null);
                if (deviceCopy != null)
                {
                    deviceCopy.setUserId(userId);
                    deviceCopyRepository.save(deviceCopy);
                    System.out.println("Device " + deviceId + " assigned to user " + userId);

                }
                else
                {
                    System.err.println("Device " + deviceId + " not found in device_copy");
                }
            }
            else if ("DEVICE_DELETED".equals(eventType))
            {
                Long deviceId = ((Number) message.get("deviceId")).longValue();
                deviceCopyRepository.deleteById(deviceId);
                System.out.println("Device ID " + deviceId + " deleted from device_copy");
            }

        } catch (Exception e) {
            System.err.println("Error processing sync event: " + e.getMessage());
            e.printStackTrace();
        }
    }
}