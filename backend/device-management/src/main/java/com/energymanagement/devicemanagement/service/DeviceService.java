package com.energymanagement.devicemanagement.service;

import com.energymanagement.devicemanagement.dto.*;
import com.energymanagement.devicemanagement.model.Device;
import com.energymanagement.devicemanagement.model.UserCopy;
import com.energymanagement.devicemanagement.repository.DeviceRepository;
import com.energymanagement.devicemanagement.repository.UserCopyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import java.util.HashMap;
import java.util.Map;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DeviceService
{

    private final DeviceRepository deviceRepository;
    private final UserCopyRepository userCopyRepository;

    // RabbitTemplate pentru publicare evenimente de sincronizare
    private final RabbitTemplate syncRabbitTemplate;

    // Numele exchange-ului de sincronizare
    @Value("${rabbitmq.exchange.sync}")
    private String syncExchangeName;

    public DeviceService(
            DeviceRepository deviceRepository,
            UserCopyRepository userCopyRepository,
            @Qualifier("syncRabbitTemplate") RabbitTemplate syncRabbitTemplate
    ) {
        this.deviceRepository = deviceRepository;
        this.userCopyRepository = userCopyRepository;
        this.syncRabbitTemplate = syncRabbitTemplate;
    }

    @Transactional
    public DeviceDTO createDevice(CreateDeviceDTO createDeviceDTO)
    {
        if (deviceRepository.existsByName(createDeviceDTO.getName()))
        {
            throw new RuntimeException("Device name already exists: " + createDeviceDTO.getName());
        }

        if (createDeviceDTO.getMaxConsumption() == null || createDeviceDTO.getMaxConsumption() <= 0)
        {
            throw new RuntimeException("Max consumption must be positive");
        }

        // Creeaza device
        Device device = new Device();
        device.setName(createDeviceDTO.getName());
        device.setMaxConsumption(BigDecimal.valueOf(createDeviceDTO.getMaxConsumption()));
        device.setUserId(null);

        Device savedDevice = deviceRepository.save(device);

        // Publica eveniment DEVICE_CREATED pe Sync Exchange
        publishDeviceCreatedEvent(savedDevice);

        return convertToDTO(savedDevice);
    }

    @Transactional
    public void deleteDevice(Long id)
    {
        if (!deviceRepository.existsById(id))
        {
            throw new RuntimeException("Device not found with id: " + id);
        }

        // Sterge device-ul
        deviceRepository.deleteById(id);

        // Publica eveniment DEVICE_DELETED pe Sync Exchange
        publishDeviceDeletedEvent(id);
    }



    public List<DeviceDTO> getAllDevices()
    {
        List<Device> devices = deviceRepository.findAll();

        return devices.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<DeviceDTO> getDeviceById(Long id)
    {
        Optional<Device> deviceOptional = deviceRepository.findById(id);

        return deviceOptional.map(this::convertToDTO);
    }

    public List<DeviceDTO> getDevicesByUserId(Long userId)
    {
        List<Device> devices = deviceRepository.findByUserId(userId);

        return devices.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public DeviceDTO updateDevice(Long id, UpdateDeviceDTO updateDeviceDTO)
    {
        Optional<Device> deviceOptional = deviceRepository.findById(id);

        if (!deviceOptional.isPresent())
        {
            throw new RuntimeException("Device not found with id: " + id);
        }

        Device device = deviceOptional.get();

        if (updateDeviceDTO.getName() != null)
        {
            device.setName(updateDeviceDTO.getName());
        }

        if (updateDeviceDTO.getMaxConsumption() != null)
        {
            device.setMaxConsumption(BigDecimal.valueOf(updateDeviceDTO.getMaxConsumption()));
        }

        Device updatedDevice = deviceRepository.save(device);

        return convertToDTO(updatedDevice);
    }

    @Transactional
    public void assignDeviceToUser(AssignDeviceDTO assignDeviceDTO)
    {
        Optional<Device> deviceOptional = deviceRepository.findById(assignDeviceDTO.getDeviceId());

        if (!deviceOptional.isPresent())
        {
            throw new RuntimeException("Device not found with id: " + assignDeviceDTO.getDeviceId());
        }

        Device device = deviceOptional.get();

        Optional<UserCopy> userCopyOptional = userCopyRepository.findById(assignDeviceDTO.getUserId());

        if (!userCopyOptional.isPresent())
        {
            throw new RuntimeException("User not found in users_copy with id: " + assignDeviceDTO.getUserId());
        }

        if (device.getUserId() != null)
        {
            throw new RuntimeException("Device is already assigned to user: " + device.getUserId());
        }

        device.setUserId(assignDeviceDTO.getUserId());
        deviceRepository.save(device);

        // Publica event DEVICE_ASSIGNED
        publishDeviceAssignedEvent(device.getId(), assignDeviceDTO.getUserId());
    }

    @Transactional
    public void deleteAllDevicesForUser(Long userId)
    {
        List<Device> devices = deviceRepository.findByUserId(userId);

        for (Device device : devices)
        {
            device.setUserId(null);
        }

        deviceRepository.saveAll(devices);
    }



    // Publica eveniment DEVICE_CREATED pe Sync Exchange
    private void publishDeviceCreatedEvent(Device device)
    {
        try {
            Map<String, Object> syncMessage = new HashMap<>();
            syncMessage.put("eventType", "DEVICE_CREATED");
            syncMessage.put("deviceId", device.getId());
            syncMessage.put("deviceName", device.getName());
            syncMessage.put("maxConsumption", device.getMaxConsumption());

            // Publica pe Sync Exchange fanout broadcast)
            syncRabbitTemplate.convertAndSend(syncExchangeName, "", syncMessage);

            System.out.println("Published DEVICE_CREATED event for device ID: " + device.getId());

        } catch (Exception e) {

            System.err.println("Failed to publish DEVICE_CREATED event: " + e.getMessage());
            e.printStackTrace();
        }
    }



    private void publishDeviceAssignedEvent(Long deviceId, Long userId)
    {
        try {
            Map<String, Object> syncMessage = new HashMap<>();
            syncMessage.put("eventType", "DEVICE_ASSIGNED");
            syncMessage.put("deviceId", deviceId);
            syncMessage.put("userId", userId);

            syncRabbitTemplate.convertAndSend(syncExchangeName, "", syncMessage);

            System.out.println("Published DEVICE_ASSIGNED event: device " + deviceId + " -> user " + userId);

        } catch (Exception e) {
            System.err.println("Failed to publish DEVICE_ASSIGNED event: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // Publica eveniment DEVICE_DELETED pe Sync Exchange
    private void publishDeviceDeletedEvent(Long deviceId)
    {
        try {
            Map<String, Object> syncMessage = new HashMap<>();
            syncMessage.put("eventType", "DEVICE_DELETED");
            syncMessage.put("deviceId", deviceId);

            // Publica pe Sync Exchange (fanout broadcast)
            syncRabbitTemplate.convertAndSend(syncExchangeName, "", syncMessage);

            System.out.println("Published DEVICE_DELETED event for device ID: " + deviceId);

        } catch (Exception e) {

            System.err.println("Failed to publish DEVICE_DELETED event: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private DeviceDTO convertToDTO(Device device) {
        return new DeviceDTO(
                device.getId(),
                device.getName(),
                device.getMaxConsumption(),
                device.getUserId()
        );
    }
}