package com.energymanagement.monitoringservice.service;

import com.energymanagement.monitoringservice.dto.DeviceMessage;
import com.energymanagement.monitoringservice.model.DeviceCopy;
import com.energymanagement.monitoringservice.model.HourlyConsumption;
import com.energymanagement.monitoringservice.repository.DeviceCopyRepository;
import com.energymanagement.monitoringservice.repository.HourlyConsumptionRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class MonitoringService
{

    private final HourlyConsumptionRepository repository;
    private final DeviceCopyRepository deviceCopyRepository;
    private final RabbitTemplate syncRabbitTemplate;

    public MonitoringService(
            HourlyConsumptionRepository repository,
            DeviceCopyRepository deviceCopyRepository,
            @Qualifier("syncRabbitTemplate") RabbitTemplate syncRabbitTemplate
    )
    {
        this.repository = repository;
        this.deviceCopyRepository = deviceCopyRepository;
        this.syncRabbitTemplate = syncRabbitTemplate;
    }

    @Transactional
    public void processDeviceMessage(DeviceMessage message)
    {
        // Validare: verifica daca device-ul exista in device_copy
        Optional<DeviceCopy> deviceCopyOpt = deviceCopyRepository.findById(message.getDeviceId());

        if (!deviceCopyOpt.isPresent())
        {
            System.err.println("ERROR: Device ID " + message.getDeviceId()
                    + " does not exist in device_copy table. Message ignored.");
            return;
        }

        DeviceCopy deviceCopy = deviceCopyOpt.get();

        // Extrage ora
        LocalDateTime hourTimestamp = message.getTimestamp()
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        // Converteste measurement value (Watts) la kWh
        double kwhValue = message.getMeasurementValue() / 6000.0;

        // cautare record existent pentru device + ora
        Optional<HourlyConsumption> existingOpt = repository.findByDeviceIdAndHourTimestamp(
                message.getDeviceId(),
                hourTimestamp
        );

        BigDecimal newTotal;

        if (existingOpt.isPresent())
        {
            // Update - adauga la total existent
            HourlyConsumption existing = existingOpt.get();
            BigDecimal currentTotal = existing.getTotalKwh();
            newTotal = currentTotal.add(BigDecimal.valueOf(kwhValue));
            existing.setTotalKwh(newTotal);
            repository.save(existing);

            System.out.println("Updated consumption for device " + message.getDeviceId()
                    + " at " + hourTimestamp + ": " + newTotal + " kWh");
        }
        else
        {
            // Insert - creare record nou
            newTotal = BigDecimal.valueOf(kwhValue);
            HourlyConsumption newRecord = new HourlyConsumption(
                    message.getDeviceId(),
                    hourTimestamp,
                    newTotal
            );
            repository.save(newRecord);

            System.out.println("Created consumption for device " + message.getDeviceId()
                    + " at " + hourTimestamp + ": " + kwhValue + " kWh");
        }

        // verificare supraconsum
        if (deviceCopy.getMaxConsumption() != null)
        {
            if (newTotal.compareTo(deviceCopy.getMaxConsumption()) > 0)
            {
                // supraconsum detectat
                publishOverconsumptionNotification(
                        message.getDeviceId(),
                        newTotal.doubleValue(),
                        deviceCopy.getMaxConsumption().doubleValue(),
                        hourTimestamp
                );
            }
        }
    }

    private void publishOverconsumptionNotification(Long deviceId, Double consumption, Double limit, LocalDateTime timestamp) {
        try {
            // Gaseste userId din device_copy
            Optional<DeviceCopy> deviceCopyOpt = deviceCopyRepository.findById(deviceId);

            if (!deviceCopyOpt.isPresent())
            {
                System.err.println("Device not found in device_copy: " + deviceId);
                return;
            }

            DeviceCopy deviceCopy = deviceCopyOpt.get();
            Long userId = deviceCopy.getUserId();

            if (userId == null)
            {
                System.err.println("Device " + deviceId + " not assigned to any user. Notification skipped.");
                return;
            }

            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "OVERCONSUMPTION");
            notification.put("deviceId", deviceId);
            notification.put("userId", userId);
            notification.put("consumption", consumption);
            notification.put("limit", limit);
            notification.put("timestamp", timestamp.toString());

            syncRabbitTemplate.convertAndSend("notifications-queue", notification);

            System.out.println("OVERCONSUMPTION ALERT sent for device " + deviceId + " (user " + userId + ")");

        } catch (Exception e) {

            System.err.println("Failed to publish overconsumption notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
}