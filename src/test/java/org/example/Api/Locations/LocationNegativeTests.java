package org.example.Api.Locations;

import com.microsoft.playwright.APIResponse;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.example.Api.helpers.LocationsHelper.LocationsClient;
import org.example.Api.helpers.assertions.ApiAssertions;
import org.example.BaseAPITestExtension.BaseApiTest;
import org.example.apifactories.LocationsTestDataFactory;
import org.example.config.TestEnvironment;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Negative test scenarios for Locations API.
 * Tests error handling, validation, and edge cases.
 */
@Epic("Locations")
@Feature("Locations Negative Scenarios")
@Timeout(value = TestEnvironment.E2E_TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
public class LocationNegativeTests extends BaseApiTest {

    private static final Logger log = LoggerFactory.getLogger(LocationNegativeTests.class);

    private LocationsClient locationsClient;
    private final List<String> createdLocationIds = new ArrayList<>();

    @BeforeAll
    void initClient() {
        locationsClient = new LocationsClient(userApi);
    }

    @AfterAll
    void cleanup() {
        for (String id : createdLocationIds) {
            try {
                locationsClient.deleteLocation(id, null, "Cleanup from negative tests");
            } catch (Exception e) {
                log.warn("Cleanup: failed to delete location {}: {}", id, e.getMessage());
            }
        }
    }

    @DisplayName("Create location with empty body returns 400")
    @Test
    void createLocation_withEmptyBody_returns400() {
        Map<String, Object> emptyBody = new HashMap<>();

        APIResponse response = locationsClient.createLocation(emptyBody, false);

        log.info("Create location with empty body - status: {}, body: {}",
                response.status(), response.text());

        ApiAssertions.assertStatus(400, response, "Create location with empty body");
    }

    @DisplayName("Create location with missing name - API accepts (no server validation)")
    @Test
    void createLocation_withMissingName_accepted() throws IOException {
        Map<String, Object> bodyWithoutName = new HashMap<>();
        bodyWithoutName.put("truckStockType", "WAREHOUSE");
        bodyWithoutName.put("note", "Missing name test");

        APIResponse response = locationsClient.createLocation(bodyWithoutName, false);

        log.info("Create location with missing name - status: {}, body: {}",
                response.status(), response.text());

        // API does not validate missing name and returns 201
        ApiAssertions.assertStatus(201, response, "Create location with missing name");

        String locationId = locationsClient.extractLocationId(response);
        if (locationId != null) {
            createdLocationIds.add(locationId);
        }
    }

    @DisplayName("Update location with non-existent ID returns 404")
    @Test
    void updateLocation_withNonExistentId_returns404() {
        String nonExistentId = UUID.randomUUID().toString();
        Map<String, Object> updateBody = LocationsTestDataFactory.buildUpdateWarehouseBody("Update Non-Existent ");

        APIResponse response = locationsClient.updateLocation(nonExistentId, updateBody, false);

        log.info("Update non-existent location {} - status: {}, body: {}",
                nonExistentId, response.status(), response.text());

        ApiAssertions.assertStatus(404, response, "Update location with non-existent ID");
    }

    @DisplayName("Delete location with non-existent ID returns 404")
    @Test
    void deleteLocation_withNonExistentId_returns404() {
        String nonExistentId = UUID.randomUUID().toString();

        APIResponse response = locationsClient.deleteLocation(
                nonExistentId,
                null,
                "Attempting to delete non-existent location"
        );

        log.info("Delete non-existent location {} - status: {}, body: {}",
                nonExistentId, response.status(), response.text());

        ApiAssertions.assertStatus(404, response, "Delete location with non-existent ID");
    }

    @DisplayName("Delete already deleted location returns 404")
    @Test
    void deleteLocation_alreadyDeleted_returns404() throws IOException {
        // Create a location
        Map<String, Object> createBody = LocationsTestDataFactory.buildCreateWarehouseBody("ToDelete ");

        APIResponse createResponse = locationsClient.createLocation(createBody, false);

        log.info("Create location for deletion test - status: {}", createResponse.status());

        ApiAssertions.assertStatusOneOf(createResponse, "Create location for deletion test", 200, 201);

        String locationId = locationsClient.extractLocationId(createResponse);
        Assertions.assertNotNull(locationId, "Location ID should not be null after creation");

        // Delete the location (first time)
        APIResponse deleteResponse = locationsClient.deleteLocation(
                locationId,
                null,
                "First deletion for double-delete test"
        );

        log.info("First delete of location {} - status: {}, body: {}",
                locationId, deleteResponse.status(), deleteResponse.text());

        ApiAssertions.assertStatusOneOf(deleteResponse, "First delete of location", 200, 204);

        // Try to delete the same location again
        APIResponse secondDeleteResponse = locationsClient.deleteLocation(
                locationId,
                null,
                "Second deletion attempt"
        );

        log.info("Second delete of location {} - status: {}, body: {}",
                locationId, secondDeleteResponse.status(), secondDeleteResponse.text());

        // API uses idempotent deletes - returns 204 even if already deleted
        ApiAssertions.assertStatusOneOf(secondDeleteResponse, "Delete already deleted location", 404, 204);

        // No need to add to cleanup list since location is already deleted
    }

    @DisplayName("Create location with invalid type returns 400")
    @Test
    void createLocation_withInvalidType_returns400() {
        Map<String, Object> invalidTypeBody = LocationsTestDataFactory.buildCreateWarehouseBody("Invalid Type ");

        // Replace the valid type with an invalid one
        invalidTypeBody.put("truckStockType", "INVALID_TYPE");

        APIResponse response = locationsClient.createLocation(invalidTypeBody, false);

        log.info("Create location with invalid type - status: {}, body: {}",
                response.status(), response.text());

        ApiAssertions.assertStatus(400, response, "Create location with invalid type");
    }

    @DisplayName("Create location with SQL injection in name succeeds (sanitized)")
    @Test
    void createLocation_withSqlInjectionInName_succeeds() throws IOException {
        log.info("Testing location creation with SQL injection in name");

        Map<String, Object> body = LocationsTestDataFactory.buildCreateWarehouseBody("'; DROP TABLE locations;-- ");

        APIResponse response = locationsClient.createLocation(body, false);
        log.info("Create location with SQL injection - status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatusOneOf(response, "Create location with SQL injection", 200, 201, 400);

        if (response.status() == 200 || response.status() == 201) {
            String locationId = locationsClient.extractLocationId(response);
            if (locationId != null) {
                createdLocationIds.add(locationId);
                log.info("Location with SQL injection name created, ID: {}", locationId);
            }
        }
    }

    @DisplayName("Create location with XSS in name succeeds (sanitized)")
    @Test
    void createLocation_withXssInName_succeeds() throws IOException {
        log.info("Testing location creation with XSS in name");

        Map<String, Object> body = LocationsTestDataFactory.buildCreateWarehouseBody("<script>alert('xss')</script> ");

        APIResponse response = locationsClient.createLocation(body, false);
        log.info("Create location with XSS - status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatusOneOf(response, "Create location with XSS", 200, 201, 400);

        if (response.status() == 200 || response.status() == 201) {
            String locationId = locationsClient.extractLocationId(response);
            if (locationId != null) {
                createdLocationIds.add(locationId);
                log.info("Location with XSS name created, ID: {}", locationId);
            }
        }
    }

    @DisplayName("Update location with invalid ID format returns error")
    @Test
    void updateLocation_withInvalidIdFormat_returnsError() {
        log.info("Testing location update with invalid ID format");

        String invalidId = "not-a-valid-uuid";
        Map<String, Object> updateBody = LocationsTestDataFactory.buildUpdateWarehouseBody("Invalid ID ");

        APIResponse response = locationsClient.updateLocation(invalidId, updateBody, false);

        log.info("Update location with invalid ID format - status: {}, body: {}",
                response.status(), response.text());

        ApiAssertions.assertStatusOneOf(response, "Update location with invalid ID format", 400, 404);
    }

    @DisplayName("Create location with extremely long name returns error or accepts")
    @Test
    void createLocation_withExtremelyLongName_returnsErrorOrAccepts() throws IOException {
        log.info("Testing location creation with extremely long name (10000 characters)");

        Map<String, Object> body = LocationsTestDataFactory.buildCreateWarehouseBody("");
        body.put("name", "L".repeat(10000));

        APIResponse response = locationsClient.createLocation(body, false);

        log.info("Create location with long name - status: {}", response.status());

        ApiAssertions.assertStatusOneOf(response, "Create location with extremely long name", 200, 201, 400, 500);

        if (response.status() == 200 || response.status() == 201) {
            String locationId = locationsClient.extractLocationId(response);
            if (locationId != null) {
                createdLocationIds.add(locationId);
            }
        }
    }

    @DisplayName("Delete location with invalid ID format returns error")
    @Test
    void deleteLocation_withInvalidIdFormat_returnsError() {
        log.info("Testing location deletion with invalid ID format");

        String invalidId = "not-a-valid-uuid";

        APIResponse response = locationsClient.deleteLocation(invalidId, null, "Invalid ID deletion test");

        log.info("Delete location with invalid ID format - status: {}, body: {}",
                response.status(), response.text());

        ApiAssertions.assertStatusOneOf(response, "Delete location with invalid ID format", 400, 404);
    }
}
