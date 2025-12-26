package org.example.apifactories;

import java.util.HashMap;
import java.util.Map;

public class LocationsTestDataFactory {

    public static Map<String, Object> buildCreateWarehouseBody(String namePrefix) {
        Map<String, Object> body = new HashMap<>();

        body.put("name", namePrefix + System.currentTimeMillis());
        body.put("truckStockType", "WAREHOUSE");
        body.put("note", "Created via tests");

        body.put("location", "123 123, dsdasd, CT, 12323");

        Map<String, Object> locationAddress = new HashMap<>();
        locationAddress.put("address", "123");
        locationAddress.put("suite", "123");
        locationAddress.put("city", "dsdasd");
        locationAddress.put("state", "CONNECTICUT");
        locationAddress.put("usZipCode", "12323");
        body.put("locationAddress", locationAddress);

        body.put("clientName", "Warehouse Client");
        body.put("clientPhoneNumber", "+10000000000");

        return body;
    }

    public static Map<String, Object> buildUpdateWarehouseBody(String namePrefix) {
        Map<String, Object> body = new HashMap<>();

        body.put("name", namePrefix + System.currentTimeMillis());
        body.put("note", "Updated via tests");

        body.put("location", "456 Updated, New City, CT, 99999");
        body.put("address", "456 Updated");
        body.put("city", "New City");
        body.put("state", "CONNECTICUT");
        body.put("suite", "999");
        body.put("usZipCode", "99999");

        body.put("clientName", "Warehouse Client UPDATED");
        body.put("clientPhoneNumber", "+19999999999");

        return body;
    }
}
