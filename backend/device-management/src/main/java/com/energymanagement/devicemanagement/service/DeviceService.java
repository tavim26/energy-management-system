package com.energymanagement.devicemanagement.service;

import com.energymanagement.devicemanagement.dto.AssignDeviceDTO;
import com.energymanagement.devicemanagement.dto.CreateDeviceDTO;
import com.energymanagement.devicemanagement.dto.DeviceDTO;
import com.energymanagement.devicemanagement.dto.UpdateDeviceDTO;
import com.energymanagement.devicemanagement.event.DeviceEventPublisher;
import com.energymanagement.devicemanagement.exception.ConflictException;
import com.energymanagement.devicemanagement.exception.ResourceNotFoundException;
import com.energymanagement.devicemanagement.model.Device;
import com.energymanagement.devicemanagement.model.UserCopy;
import com.energymanagement.devicemanagement.repository.DeviceRepository;
import com.energymanagement.devicemanagement.repository.UserCopyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final UserCopyRepository userCopyRepository;
    private final DeviceEventPublisher eventPublisher;

    public DeviceService(
            DeviceRepository deviceRepository,
            UserCopyRepository userCopyRepository,
            DeviceEventPublisher eventPublisher
    ) {
        this.deviceRepository = deviceRepository;
        this.userCopyRepository = userCopyRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public List<DeviceDTO> getAllDevices() {
        return deviceRepository.findAll().stream()
                .map(DeviceDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DeviceDTO getDeviceById(Long id) {
        return DeviceDTO.from(findDevice(id));
    }

    @Transactional(readOnly = true)
    public List<DeviceDTO> getDevicesByUserId(Long userId) {
        return deviceRepository.findByUserId(userId).stream()
                .map(DeviceDTO::from)
                .toList();
    }

    @Transactional
    public DeviceDTO createDevice(CreateDeviceDTO request) {
        String name = request.name().trim();
        ensureNameIsAvailable(name);

        Device device = deviceRepository.save(new Device(name, request.maxConsumption()));
        eventPublisher.deviceCreated(device);

        return DeviceDTO.from(device);
    }

    @Transactional
    public DeviceDTO updateDevice(Long id, UpdateDeviceDTO request) {
        Device device = findDevice(id);

        if (request.name() != null) {
            String name = request.name().trim();

            if (name.isEmpty()) {
                throw new IllegalArgumentException("Device name cannot be empty");
            }

            if (!name.equals(device.getName())) {
                ensureNameIsAvailable(name);
                device.setName(name);
            }
        }

        if (request.maxConsumption() != null) {
            device.setMaxConsumption(request.maxConsumption());
        }

        Device saved = deviceRepository.save(device);
        eventPublisher.deviceUpdated(saved);

        return DeviceDTO.from(saved);
    }

    @Transactional
    public void deleteDevice(Long id) {
        Device device = findDevice(id);

        deviceRepository.delete(device);
        eventPublisher.deviceDeleted(device.getId());
    }

    @Transactional
    public void assignDevice(AssignDeviceDTO request) {
        Device device = findDevice(request.deviceId());

        if (!userCopyRepository.existsById(request.userId())) {
            throw new ResourceNotFoundException(
                    "User " + request.userId() + " was not found or cannot own devices");
        }

        if (device.getUserId() != null) {
            throw new ConflictException(
                    "Device '" + device.getName() + "' is already assigned to user " + device.getUserId());
        }

        device.setUserId(request.userId());
        deviceRepository.save(device);
        eventPublisher.deviceAssigned(device.getId(), request.userId());
    }

    @Transactional
    public void unassignDevice(Long deviceId) {
        Device device = findDevice(deviceId);

        if (device.getUserId() == null) {
            throw new ConflictException("Device '" + device.getName() + "' is not assigned to any user");
        }

        device.setUserId(null);
        deviceRepository.save(device);
        eventPublisher.deviceUnassigned(device.getId());
    }

    // Called when a USER_CREATED event arrives for a CLIENT account
    @Transactional
    public void registerUser(Long userId) {
        if (!userCopyRepository.existsById(userId)) {
            userCopyRepository.save(new UserCopy(userId));
        }
    }

    // Called when a USER_DELETED event arrives: the user's devices become free again
    @Transactional
    public void removeUser(Long userId) {
        List<Device> devices = deviceRepository.findByUserId(userId);

        for (Device device : devices) {
            device.setUserId(null);
            eventPublisher.deviceUnassigned(device.getId());
        }

        deviceRepository.saveAll(devices);
        userCopyRepository.deleteById(userId);
    }

    private Device findDevice(Long id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device " + id + " was not found"));
    }

    private void ensureNameIsAvailable(String name) {
        if (deviceRepository.existsByName(name)) {
            throw new ConflictException("A device named '" + name + "' already exists");
        }
    }
}