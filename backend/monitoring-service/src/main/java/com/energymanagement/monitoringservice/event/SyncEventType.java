package com.energymanagement.monitoringservice.event;

public final class SyncEventType {

    public static final String DEVICE_CREATED = "DEVICE_CREATED";
    public static final String DEVICE_UPDATED = "DEVICE_UPDATED";
    public static final String DEVICE_ASSIGNED = "DEVICE_ASSIGNED";
    public static final String DEVICE_UNASSIGNED = "DEVICE_UNASSIGNED";
    public static final String DEVICE_DELETED = "DEVICE_DELETED";

    private SyncEventType() {
    }
}