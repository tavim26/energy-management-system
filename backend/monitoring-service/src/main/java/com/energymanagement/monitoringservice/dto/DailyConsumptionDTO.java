package com.energymanagement.monitoringservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// Consumption for each of the 24 hours of a day; hours without data have 0 kWh
public record DailyConsumptionDTO(Long deviceId, LocalDate date, List<HourValue> hours, BigDecimal totalKwh) {

    public record HourValue(int hour, BigDecimal kwh) {
    }
}