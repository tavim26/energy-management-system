package com.energymanagement.devicemanagement.dto;

 // DTO pentru asignarea unui device la un user
 // POST /api/devices/assign
public class AssignDeviceDTO
 {

    private Long userId;
    private Long deviceId;

    public AssignDeviceDTO() {
    }

    public AssignDeviceDTO(Long userId, Long deviceId) {
        this.userId = userId;
        this.deviceId = deviceId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }
}