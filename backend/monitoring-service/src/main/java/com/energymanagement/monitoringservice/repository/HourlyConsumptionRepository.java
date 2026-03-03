package com.energymanagement.monitoringservice.repository;

import com.energymanagement.monitoringservice.model.HourlyConsumption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HourlyConsumptionRepository extends JpaRepository<HourlyConsumption, Long>
{

    // Gaseste record pentru device + ora specifica
    Optional<HourlyConsumption> findByDeviceIdAndHourTimestamp(Long deviceId, LocalDateTime hourTimestamp);

    // Gaseste toate recordurile pentru un device intr-o zi
    List<HourlyConsumption> findByDeviceIdAndHourTimestampBetween(
            Long deviceId,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );
}