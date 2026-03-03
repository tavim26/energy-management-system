package com.energymanagement.devicemanagement.dto;

public class UpdateDeviceDTO
{

    private String name;
    private Double maxConsumption;

    public UpdateDeviceDTO() {
    }

    public UpdateDeviceDTO(String name, Double maxConsumption) {
        this.name = name;
        this.maxConsumption = maxConsumption;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getMaxConsumption() {
        return maxConsumption;
    }

    public void setMaxConsumption(Double maxConsumption) {
        this.maxConsumption = maxConsumption;
    }
}