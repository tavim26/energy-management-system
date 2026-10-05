package com.energymanagement.monitoringservice.service;

import com.energymanagement.monitoringservice.dto.DailyConsumptionDTO;
import com.energymanagement.monitoringservice.dto.DeviceMessage;
import com.energymanagement.monitoringservice.dto.HourlyConsumptionDTO;
import com.energymanagement.monitoringservice.dto.MonitoredDeviceDTO;
import com.energymanagement.monitoringservice.event.OverconsumptionPublisher;
import com.energymanagement.monitoringservice.exception.ResourceNotFoundException;
import com.energymanagement.monitoringservice.model.DeviceCopy;
import com.energymanagement.monitoringservice.model.HourlyConsumption;
import com.energymanagement.monitoringservice.repository.DeviceCopyRepository;
import com.energymanagement.monitoringservice.repository.HourlyConsumptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MonitoringService {

    private static final Logger log = LoggerFactory.getLogger(MonitoringService.class);

    // A measurement is the average power over 10 minutes (1/6 h).
    // Energy = power * time = (W / 1000) kW * (1/6) h = W / 6000 kWh
    private static final BigDecimal WATTS_TO_KWH_PER_MEASUREMENT = BigDecimal.valueOf(6000);

    private static final int MAX_LATEST_RECORDS = 100;

    private final HourlyConsumptionRepository consumptionRepository;
    private final DeviceCopyRepository deviceCopyRepository;
    private final OverconsumptionPublisher overconsumptionPublisher;

    public MonitoringService(
            HourlyConsumptionRepository consumptionRepository,
            DeviceCopyRepository deviceCopyRepository,
            OverconsumptionPublisher overconsumptionPublisher
    ) {
        this.consumptionRepository = consumptionRepository;
        this.deviceCopyRepository = deviceCopyRepository;
        this.overconsumptionPublisher = overconsumptionPublisher;
    }

    // Adds a measurement to the hourly total of its device and checks the hourly limit
    @Transactional
    public void processMeasurement(DeviceMessage message) {
        if (message.deviceId() == null || message.timestamp() == null
                || message.measurementValue() == null || message.measurementValue() < 0) {
            log.warn("Invalid measurement ignored: {}", message);
            return;
        }

        DeviceCopy device = deviceCopyRepository.findById(message.deviceId()).orElse(null);

        if (device == null) {
            log.warn("Measurement ignored: device {} is not known by the monitoring service", message.deviceId());
            return;
        }

        LocalDateTime hour = message.timestamp().truncatedTo(ChronoUnit.HOURS);

        HourlyConsumption consumption = consumptionRepository
                .findByDeviceIdAndHourTimestamp(device.getDeviceId(), hour)
                .orElseGet(() -> new HourlyConsumption(device.getDeviceId(), hour));

        BigDecimal kwh = BigDecimal.valueOf(message.measurementValue())
                .divide(WATTS_TO_KWH_PER_MEASUREMENT, 4, RoundingMode.HALF_UP);

        consumption.addKwh(kwh);

        if (shouldSendAlert(device, consumption)) {
            consumption.markAlertSent();
            overconsumptionPublisher.publish(device, consumption);
        }

        consumptionRepository.save(consumption);

        log.debug("Device {} at {}: {} kWh", device.getDeviceId(), hour, consumption.getTotalKwh());
    }

    @Transactional(readOnly = true)
    public DailyConsumptionDTO getDailyConsumption(Long deviceId, LocalDate date) {
        ensureDeviceExists(deviceId);

        Map<Integer, BigDecimal> kwhByHour = consumptionRepository
                .findInInterval(deviceId, date.atStartOfDay(), date.plusDays(1).atStartOfDay())
                .stream()
                .collect(Collectors.toMap(
                        record -> record.getHourTimestamp().getHour(),
                        HourlyConsumption::getTotalKwh
                ));

        List<DailyConsumptionDTO.HourValue> hours = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (int hour = 0; hour < 24; hour++) {
            BigDecimal kwh = kwhByHour.getOrDefault(hour, BigDecimal.ZERO);
            hours.add(new DailyConsumptionDTO.HourValue(hour, kwh));
            total = total.add(kwh);
        }

        return new DailyConsumptionDTO(deviceId, date, hours, total);
    }

    @Transactional(readOnly = true)
    public List<HourlyConsumptionDTO> getLatestConsumption(Long deviceId, int limit) {
        if (limit < 1 || limit > MAX_LATEST_RECORDS) {
            throw new IllegalArgumentException("Limit must be between 1 and " + MAX_LATEST_RECORDS);
        }

        ensureDeviceExists(deviceId);

        return consumptionRepository
                .findByDeviceIdOrderByHourTimestampDesc(deviceId, PageRequest.of(0, limit))
                .stream()
                .map(record -> new HourlyConsumptionDTO(record.getHourTimestamp(), record.getTotalKwh()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MonitoredDeviceDTO> getMonitoredDevices() {
        return deviceCopyRepository.findAll().stream()
                .map(MonitoredDeviceDTO::from)
                .toList();
    }

    // Alerts go only to the owner, once per hour, when the hourly total goes over the device limit
    private boolean shouldSendAlert(DeviceCopy device, HourlyConsumption consumption) {
        return !consumption.isAlertSent()
                && device.getUserId() != null
                && device.getMaxConsumption() != null
                && consumption.getTotalKwh().compareTo(device.getMaxConsumption()) > 0;
    }

    private void ensureDeviceExists(Long deviceId) {
        if (!deviceCopyRepository.existsById(deviceId)) {
            throw new ResourceNotFoundException("Device " + deviceId + " was not found");
        }
    }
}