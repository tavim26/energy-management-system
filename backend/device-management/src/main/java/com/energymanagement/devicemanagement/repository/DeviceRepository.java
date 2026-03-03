package com.energymanagement.devicemanagement.repository;

import com.energymanagement.devicemanagement.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    // Verifica daca exista device cu numele dat
    boolean existsByName(String name);

    // Gaseste toate device-urile alocate unui user
    List<Device> findByUserId(Long userId);

    // Gaseste toate device-urile NEALOCATE (userId = NULL)
    List<Device> findByUserIdIsNull();
}