package org.example.apifactories;

import java.util.HashMap;
import java.util.Map;

public class JobLocationsTestDataFactory {

    private JobLocationsTestDataFactory() {
        // utility class
    }

    // POST /locations – з nested locationAddress
    public static Map<String, Object> buildCreateJobBody(String namePrefix) {
        Map<String, Object> body = new HashMap<>();

        body.put("name", namePrefix + System.currentTimeMillis());
        body.put("truckStockType", "JOB");
        body.put("clientName", "API Test Client");
        body.put("clientPhoneNumber", "+10000000000");
        body.put("note", "Created via Playwright API E2E test");

        Map<String, Object> locationAddress = new HashMap<>();
        locationAddress.put("address", "123 Test Street");
        locationAddress.put("city", "Test City");
        locationAddress.put("state", "ALABAMA");
        locationAddress.put("suite", "Suite 1");
        locationAddress.put("usZipCode", "12345");
        body.put("locationAddress", locationAddress);

        return body;
    }

    // PUT /locations/{id} – пласка адреса
    public static Map<String, Object> buildUpdateJobBody(String namePrefix) {
        Map<String, Object> body = new HashMap<>();

        body.put("name", namePrefix + System.currentTimeMillis());
        body.put("clientName", "API Test Client UPDATED");
        body.put("clientPhoneNumber", "+19999999999");
        body.put("note", "Updated via Playwright API E2E test");

        body.put("address", "456 Updated Street");
        body.put("city", "Updated City");
        body.put("state", "ALABAMA");
        body.put("suite", "Updated Suite");
        body.put("usZipCode", "54321");

        body.put("location", "Updated Job Location");

        return body;
    }
}
