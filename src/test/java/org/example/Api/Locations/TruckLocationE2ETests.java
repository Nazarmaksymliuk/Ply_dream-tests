package org.example.Api.Locations;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.APIResponse;
import org.example.Api.helpers.LocationsHelper.LocationsClient;
import org.example.BaseAPITestExtension.BaseApiTest;
import org.example.apifactories.TruckLocationsTestDataFactory;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.Map;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TruckLocationE2ETests extends BaseApiTest {

    private LocationsClient locationsClient;
    private String locationId;

    @BeforeAll
    void initClient() {
        locationsClient = new LocationsClient(userApi);
    }

    // 1️⃣ CREATE TRUCK
    @Test
    @Order(1)
    void createTruck_createsAndStoresId() throws IOException {
        Map<String, Object> body =
                TruckLocationsTestDataFactory.buildCreateTruckBody("API Truck ");

        APIResponse response = locationsClient.createLocation(body, false);
        int status = response.status();

        System.out.println("CREATE TRUCK status: " + status);
        System.out.println("CREATE TRUCK body: " + response.text());

        // swagger: 201 Created, але допустимо 200 на всякий
        Assertions.assertTrue(
                status == 201 || status == 200,
                "Expected 201 or 200 on create, but got: " + status
        );

        locationId = locationsClient.extractLocationId(response);
        Assertions.assertNotNull(locationId, "locationId must not be null after create");
        Assertions.assertFalse(locationId.isEmpty(), "locationId must not be empty");

        JsonNode created = locationsClient.parseLocation(response);
        Assertions.assertEquals("TRUCK", created.get("truckStockType").asText());
        Assertions.assertEquals("123", created.get("plateNumber").asText());
        Assertions.assertEquals("123", created.get("make").asText());
        Assertions.assertEquals("123", created.get("model").asText());
    }

    // 2️⃣ UPDATE TRUCK
    @Test
    @Order(2)
    void updateTruck_updatesNameAndPlateAndMakeModel() throws IOException {
        Assertions.assertNotNull(locationId, "locationId is null – create test probably failed");

        Map<String, Object> body =
                TruckLocationsTestDataFactory.buildUpdateTruckBody("API Truck UPDATED ");

        APIResponse response = locationsClient.updateLocation(locationId, body, true);
        int status = response.status();

        System.out.println("UPDATE TRUCK status: " + status);
        System.out.println("UPDATE TRUCK body: " + response.text());

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
        Assertions.assertEquals(
                body.get("plateNumber"),
                updated.get("plateNumber").asText(),
                "Updated plateNumber must match request"
        );
        Assertions.assertEquals(
                body.get("make"),
                updated.get("make").asText(),
                "Updated make must match request"
        );
        Assertions.assertEquals(
                body.get("model"),
                updated.get("model").asText(),
                "Updated model must match request"
        );
    }

    // 3️⃣ DELETE TRUCK
    @Test
    @Order(3)
    void deleteTruck_deletesPreviouslyCreated() {
        Assertions.assertNotNull(locationId, "locationId is null – create test probably failed");

        APIResponse response = locationsClient.deleteLocation(
                locationId,
                null, // toLocationId не передаємо — нічого не переносимо
                "Delete truck via API E2E test"
        );

        int status = response.status();

        System.out.println("DELETE TRUCK status: " + status);
        System.out.println("DELETE TRUCK body: '" + response.text() + "'");

        // swagger: 204 No Content, але інколи 200
        Assertions.assertTrue(
                status == 204 || status == 200,
                "Expected 204 or 200 on delete, but got: " + status
        );
    }
}
