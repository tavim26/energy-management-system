package com.energymanagement.monitoringservice.repository;

import com.energymanagement.monitoringservice.model.DeviceCopy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceCopyRepository extends JpaRepository<DeviceCopy, Long> {
}