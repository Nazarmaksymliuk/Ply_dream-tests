package org.example.apifactories;

import java.util.HashMap;
import java.util.Map;

public class TruckLocationsTestDataFactory {

    private TruckLocationsTestDataFactory() {
        // utility class
    }

    public static Map<String, Object> buildCreateTruckBody(String namePrefix) {
        Map<String, Object> body = new HashMap<>();

        body.put("name", namePrefix + System.currentTimeMillis());
        body.put("truckStockType", "TRUCK");
        body.put("plateNumber", "123");
        body.put("make", "123");
        body.put("model", "123");

        body.put("note", "Created via API Truck E2E test");

        // address/locationAddress не ставимо — як у тебе з UI (null)
        return body;
    }

    public static Map<String, Object> buildUpdateTruckBody(String namePrefix) {
        Map<String, Object> body = new HashMap<>();

        body.put("name", namePrefix + System.currentTimeMillis());
        body.put("plateNumber", "TRK-9999");
        body.put("make", "Ford");
        body.put("model", "F-150");

        body.put("note", "Updated via API Truck E2E test");

        return body;
    }
}
