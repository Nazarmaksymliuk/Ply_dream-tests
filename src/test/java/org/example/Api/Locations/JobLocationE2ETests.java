package org.example.Api.Locations;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.APIResponse;
import org.example.Api.helpers.LocationsHelper.LocationsClient;
import org.example.BaseAPITestExtension.BaseApiTest;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JobLocationE2ETests extends BaseApiTest {

    private LocationsClient locationsClient;
    private String locationId;

    @BeforeAll
    void initClient() {
        locationsClient = new LocationsClient(apiRequest);
    }

    // 1️⃣ CREATE JOB LOCATION
    @Test
    @Order(1)
    void createJobLocation_createsAndStoresId() throws IOException {
        Map<String, Object> body = buildCreateJobBody();

        APIResponse response = locationsClient.createLocation(body, false);
        int status = response.status();

        System.out.println("CREATE JOB status: " + status);
        System.out.println("CREATE JOB body: " + response.text());

        // swagger каже 201, але іноді буває 200 → допускаємо обидва
        Assertions.assertTrue(
                status == 201 || status == 200,
                "Expected 201 or 200 on create, but got: " + status
        );

        locationId = locationsClient.extractLocationId(response);
        Assertions.assertNotNull(locationId, "locationId must not be null after create");
        Assertions.assertFalse(locationId.isEmpty(), "locationId must not be empty");

        JsonNode location = locationsClient.parseLocation(response);
        Assertions.assertEquals("JOB", location.get("truckStockType").asText());
    }

    // 2️⃣ UPDATE JOB LOCATION
    @Test
    @Order(2)
    void updateJobLocation_updatesNameAndClientData() throws IOException {
        Assertions.assertNotNull(locationId, "locationId is null – create test probably failed");

        Map<String, Object> body = buildUpdateJobBody();

        APIResponse response = locationsClient.updateLocation(locationId, body, true);
        int status = response.status();

        System.out.println("UPDATE JOB status: " + status);
        System.out.println("UPDATE JOB body: " + response.text());

        // swagger: 200 / 201
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
                body.get("clientName"),
                updated.get("clientName").asText(),
                "Updated clientName must match request"
        );
    }

    // 3️⃣ DELETE JOB LOCATION
    @Test
    @Order(3)
    void deleteJobLocation_deletesPreviouslyCreated() {
        Assertions.assertNotNull(locationId, "locationId is null – create test probably failed");

        APIResponse response = locationsClient.deleteLocation(
                locationId,
                null, // toLocationId – не вказуємо, якщо не потрібно переносити запаси
                "Delete via API E2E test"
        );

        int status = response.status();

        System.out.println("DELETE JOB status: " + status);
        System.out.println("DELETE JOB body: '" + response.text() + "'");

        // swagger каже 204 No Content, але на всяк випадок допускаємо 200 теж
        Assertions.assertTrue(
                status == 204 || status == 200,
                "Expected 204 or 200 on delete, but got: " + status
        );
    }

    // ---------- helpers ----------

    private Map<String, Object> buildCreateJobBody() {
        Map<String, Object> body = new HashMap<>();

        body.put("name", "API Job " + System.currentTimeMillis());
        body.put("truckStockType", "JOB"); // важливо, щоб це був саме Job
        body.put("clientName", "API Test Client");
        body.put("clientPhoneNumber", "+10000000000");
        body.put("note", "Created via Playwright API E2E test");

        // мінімальна адреса – nested locationAddress (як в POST схемі)
        Map<String, Object> locationAddress = new HashMap<>();
        locationAddress.put("address", "123 Test Street");
        locationAddress.put("city", "Test City");
        locationAddress.put("state", "ALABAMA"); // з enum у swagger
        locationAddress.put("suite", "Suite 1");
        locationAddress.put("usZipCode", "12345");
        body.put("locationAddress", locationAddress);

        // решта полів (projectId, truckId, usersId...) можна не чіпати, якщо бекенд їх не вимагає

        return body;
    }

    private Map<String, Object> buildUpdateJobBody() {
        Map<String, Object> body = new HashMap<>();

        body.put("name", "API Job UPDATED " + System.currentTimeMillis());
        body.put("clientName", "API Test Client UPDATED");
        body.put("clientPhoneNumber", "+19999999999");
        body.put("note", "Updated via Playwright API E2E test");

        // PUT має пласку адресу, без locationAddress-обʼєкта
        body.put("address", "456 Updated Street");
        body.put("city", "Updated City");
        body.put("state", "ALABAMA");
        body.put("suite", "Updated Suite");
        body.put("usZipCode", "54321");

        // можеш додати location, make, model, якщо хочеш, але не обовʼязково
        body.put("location", "Updated Job Location");

        return body;
    }
}
