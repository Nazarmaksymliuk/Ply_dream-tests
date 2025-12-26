package org.example.Api.Locations;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.APIResponse;
import org.example.Api.helpers.LocationsHelper.LocationsClient;
import org.example.BaseAPITestExtension.BaseApiTest;
import org.example.apifactories.LocationsTestDataFactory;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.Map;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WarehouseLocationE2ETests extends BaseApiTest {

    private LocationsClient locationsClient;
    private String locationId;

    @BeforeAll
    void initClient() {
        locationsClient = new LocationsClient(userApi);
    }

    // 1️⃣ CREATE WAREHOUSE
    @Test
    @Order(1)
    void createWarehouse_createsAndStoresId() throws IOException {
        Map<String, Object> body =
                LocationsTestDataFactory.buildCreateWarehouseBody("API Warehouse ");

        APIResponse response = locationsClient.createLocation(body, false);
        int status = response.status();

        System.out.println("CREATE WAREHOUSE status: " + status);
        System.out.println("CREATE WAREHOUSE body: " + response.text());

        // swagger каже 201, але інколи буває 200
        Assertions.assertTrue(
                status == 201 || status == 200,
                "Expected 201 or 200 on create, but got: " + status
        );

        locationId = locationsClient.extractLocationId(response);
        Assertions.assertNotNull(locationId, "locationId must not be null after create");
        Assertions.assertFalse(locationId.isEmpty(), "locationId must not be empty");

        JsonNode created = locationsClient.parseLocation(response);
        Assertions.assertEquals("WAREHOUSE", created.get("truckStockType").asText());
        Assertions.assertEquals("AQA MAIN BUSINESS", created.get("businessName").asText());
    }

    // 2️⃣ UPDATE WAREHOUSE
    @Test
    @Order(2)
    void updateWarehouse_updatesNameAndLocation() throws IOException {
        Assertions.assertNotNull(locationId, "locationId is null – create test probably failed");

        Map<String, Object> body =
                LocationsTestDataFactory.buildUpdateWarehouseBody("API Warehouse UPDATED ");

        APIResponse response = locationsClient.updateLocation(locationId, body, true);
        int status = response.status();

        System.out.println("UPDATE WAREHOUSE status: " + status);
        System.out.println("UPDATE WAREHOUSE body: " + response.text());

        Assertions.assertTrue(
                status == 200 || status == 201,
                "Expected 200 or 201 on update, but got: " + status
        );

        JsonNode updated = locationsClient.parseLocation(response);

        Assertions.assertEquals(locationId, updated.get("id").asText(), "Updated id must be the same");
        Assertions.assertEquals(
                body.get("name"),
                updated.get("name").asText(),
                "Updated name must match request"
        );

        // перевіряємо структуровану адресу, яка повертається як locationAddress
        JsonNode addr = updated.get("locationAddress");
        Assertions.assertNotNull(addr, "locationAddress should be present in response");

        Assertions.assertEquals(body.get("address"),   addr.get("address").asText(),   "address mismatch");
        Assertions.assertEquals(body.get("suite"),     addr.get("suite").asText(),     "suite mismatch");
        Assertions.assertEquals(body.get("city"),      addr.get("city").asText(),      "city mismatch");
        Assertions.assertEquals(body.get("state"),     addr.get("state").asText(),     "state mismatch");
        Assertions.assertEquals(body.get("usZipCode"), addr.get("usZipCode").asText(), "zip mismatch");

        // опційно: мʼяка перевірка для зліпленого location
        String locationStr = updated.get("location").asText();
        Assertions.assertTrue(
                locationStr.contains((String) body.get("address")),
                "location should contain address"
        );
        Assertions.assertTrue(
                locationStr.contains((String) body.get("city")),
                "location should contain city"
        );
        Assertions.assertTrue(
                locationStr.contains((String) body.get("usZipCode")),
                "location should contain zip"
        );
    }

    // 3️⃣ DELETE WAREHOUSE
    @Test
    @Order(3)
    void deleteWarehouse_deletesPreviouslyCreated() {
        Assertions.assertNotNull(locationId, "locationId is null – create test probably failed");

        APIResponse response = locationsClient.deleteLocation(
                locationId,
                null, // toLocationId не передаємо — нічого не переносимо
                "Delete warehouse via API E2E test"
        );

        int status = response.status();

        System.out.println("DELETE WAREHOUSE status: " + status);
        System.out.println("DELETE WAREHOUSE body: '" + response.text() + "'");

        // swagger: 204 No Content, але інколи 200
        Assertions.assertTrue(
                status == 204 || status == 200,
                "Expected 204 or 200 on delete, but got: " + status
        );
    }
}
