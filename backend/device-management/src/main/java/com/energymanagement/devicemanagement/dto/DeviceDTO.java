package com.energymanagement.devicemanagement.dto;

import java.math.BigDecimal;


public class DeviceDTO {

    private Long id;
    private String name;
    private BigDecimal maxConsumption;
    private Long userId;

    public DeviceDTO() {
    }

    public DeviceDTO(Long id, String name, BigDecimal maxConsumption, Long userId) {
        this.id = id;
        this.name = name;
        this.maxConsumption = maxConsumption;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getMaxConsumption() {
        return maxConsumption;
    }

    public void setMaxConsumption(BigDecimal maxConsumption) {
        this.maxConsumption = maxConsumption;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}