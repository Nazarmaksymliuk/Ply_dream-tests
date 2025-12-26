package org.example.Api.Locations;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.APIResponse;
import org.example.Api.helpers.LocationsHelper.LocationsClient;
import org.example.BaseAPITestExtension.BaseApiTest;
import org.example.apifactories.JobLocationsTestDataFactory;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.Map;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JobLocationE2ETests extends BaseApiTest {

    private LocationsClient locationsClient;
    private String locationId;

    @BeforeAll
    void initClient() {
        locationsClient = new LocationsClient(userApi);
    }

    // 1️⃣ CREATE JOB LOCATION
    @Test
    @Order(1)
    void createJobLocation_createsAndStoresId() throws IOException {
        Map<String, Object> body =
                JobLocationsTestDataFactory.buildCreateJobBody("API Job ");

        APIResponse response = locationsClient.createLocation(body, false);
        int status = response.status();

        System.out.println("CREATE JOB status: " + status);
        System.out.println("CREATE JOB body: " + response.text());

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

        Map<String, Object> body =
                JobLocationsTestDataFactory.buildUpdateJobBody("API Job UPDATED ");

        APIResponse response = locationsClient.updateLocation(locationId, body, true);
        int status = response.status();

        System.out.println("UPDATE JOB status: " + status);
        System.out.println("UPDATE JOB body: " + response.text());

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
                null,
                "Delete via API E2E test"
        );

        int status = response.status();

        System.out.println("DELETE JOB status: " + status);
        System.out.println("DELETE JOB body: '" + response.text() + "'");

        Assertions.assertTrue(
                status == 204 || status == 200,
                "Expected 204 or 200 on delete, but got: " + status
        );
    }
}
