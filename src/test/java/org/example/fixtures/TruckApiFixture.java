package org.example.fixtures;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import org.example.Api.helpers.LocationsHelper.LocationsClient;
import org.example.apifactories.TruckLocationsTestDataFactory;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.util.Map;

/**
 * Reusable fixture: creates a truck (location) via API and deletes it in cleanup().
 * Intended to be reused across UI/API tests.
 */
public class TruckApiFixture {

    private final LocationsClient locationsClient;

    private String truckId;
    private String truckName;

    private TruckApiFixture(LocationsClient locationsClient) {
        this.locationsClient = locationsClient;
    }

    public static TruckApiFixture create(APIRequestContext requestContext) {
        return new TruckApiFixture(new LocationsClient(requestContext));
    }

    /**
     * Creates a truck via TruckLocationsTestDataFactory.buildCreateTruckBody(prefix)
     *
     * @param namePrefix prefix for generated truck name
     */
    public TruckApiFixture provisionTruck(String namePrefix) throws IOException {
        Map<String, Object> body = TruckLocationsTestDataFactory.buildCreateTruckBody(namePrefix);

        APIResponse resp = locationsClient.createLocation(body, false);
        assertStatus(resp, "CREATE TRUCK", 200, 201);

        JsonNode created = locationsClient.parseLocation(resp);
        truckId = textOrNull(created, "id");
        truckName = textOrNull(created, "name");

        assertNotBlank("truckId", truckId);
        assertNotBlank("truckName", truckName);

        return this;
    }

    /**
     * Deletes created truck if present.
     *
     * @param reason reason text sent in delete request body
     */
    public void cleanup(String reason) {
        if (!isNotBlank(truckId)) return;

        try {
            APIResponse resp = locationsClient.deleteLocation(
                    truckId,
                    null,
                    (reason == null || reason.isBlank())
                            ? "Cleanup after test (truck created via API)"
                            : reason
            );
            assertStatus(resp, "DELETE TRUCK", 200, 204);
        } catch (Exception e) {
            System.out.println("DELETE TRUCK failed: " + e.getMessage());
        }
    }

    public String truckId() {
        return truckId;
    }

    public String truckName() {
        return truckName;
    }

    // -------------------------
    // Helpers
    // -------------------------

    private static void assertStatus(APIResponse resp, String step, int... okStatuses) {
        int actual = resp.status();
        for (int s : okStatuses) {
            if (actual == s) return;
        }
        Assertions.fail(step + " unexpected status: " + actual + ", body: " + resp.text());
    }

    private static void assertNotBlank(String field, String value) {
        Assertions.assertNotNull(value, field + " is null");
        Assertions.assertFalse(value.isBlank(), field + " is blank");
    }

    private static boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }

    private static String textOrNull(JsonNode node, String field) {
        JsonNode v = node == null ? null : node.get(field);
        return (v == null || v.isNull()) ? null : v.asText();
    }
}
