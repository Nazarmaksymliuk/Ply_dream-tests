package org.example.domain;

public enum LocationName {
    WAREHOUSE_MAIN("WarehouseMain"),
    WAREHOUSE_TO_TRANSFER("WarehouseToTransfer"),
    TRUCK_MAIN("TruckMain"),
    MAIN_JOB("MainJob");

    private final String value;

    LocationName(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
