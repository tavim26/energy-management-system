package com.energymanagement.devicemanagement.repository;

import com.energymanagement.devicemanagement.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    boolean existsByName(String name);

    List<Device> findByUserId(Long userId);
}