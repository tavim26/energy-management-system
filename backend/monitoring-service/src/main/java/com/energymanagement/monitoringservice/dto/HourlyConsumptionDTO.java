package com.energymanagement.monitoringservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HourlyConsumptionDTO(LocalDateTime hour, BigDecimal kwh) {
}