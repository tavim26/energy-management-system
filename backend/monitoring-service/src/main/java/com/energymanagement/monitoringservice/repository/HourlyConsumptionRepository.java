package com.energymanagement.monitoringservice.repository;

import com.energymanagement.monitoringservice.model.HourlyConsumption;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface HourlyConsumptionRepository extends JpaRepository<HourlyConsumption, Long> {

    Optional<HourlyConsumption> findByDeviceIdAndHourTimestamp(Long deviceId, LocalDateTime hourTimestamp);

    // The end of the interval is excluded, so the next day's 00:00 hour is not included
    @Query("""
            select h from HourlyConsumption h
            where h.deviceId = :deviceId and h.hourTimestamp >= :from and h.hourTimestamp < :to
            order by h.hourTimestamp
            """)
    List<HourlyConsumption> findInInterval(
            @Param("deviceId") Long deviceId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    List<HourlyConsumption> findByDeviceIdOrderByHourTimestampDesc(Long deviceId, Pageable pageable);

    @Modifying
    @Query("delete from HourlyConsumption h where h.deviceId = :deviceId")
    void deleteByDeviceId(@Param("deviceId") Long deviceId);
}