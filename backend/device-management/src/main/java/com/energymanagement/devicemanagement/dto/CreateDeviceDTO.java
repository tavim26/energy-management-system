package com.energymanagement.devicemanagement.dto;

public class CreateDeviceDTO
{

    private String name;
    private Double maxConsumption;

    public CreateDeviceDTO() {
    }

    public CreateDeviceDTO(String name, Double maxConsumption) {
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