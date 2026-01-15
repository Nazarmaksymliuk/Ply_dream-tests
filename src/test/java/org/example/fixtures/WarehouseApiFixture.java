package org.example.fixtures;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import org.example.Api.helpers.LocationsHelper.LocationsClient;
import org.example.apifactories.LocationsTestDataFactory;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.util.Map;

/**
 * Reusable fixture: creates a warehouse (location) via API and deletes it in cleanup().
 * Can be reused across UI/API tests.
 */
public class WarehouseApiFixture {

    private final LocationsClient locationsClient;

    private String warehouseId;
    private String warehouseName;

    private WarehouseApiFixture(LocationsClient locationsClient) {
        this.locationsClient = locationsClient;
    }

    public static WarehouseApiFixture create(APIRequestContext requestContext) {
        return new WarehouseApiFixture(new LocationsClient(requestContext));
    }

    /**
     * Creates a warehouse via LocationsTestDataFactory.buildCreateWarehouseBody(prefix).
     *
     * @param namePrefix prefix for generated warehouse name
     */
    public WarehouseApiFixture provisionWarehouse(String namePrefix) throws IOException {
        Map<String, Object> body = LocationsTestDataFactory.buildCreateWarehouseBody(namePrefix);

        APIResponse resp = locationsClient.createLocation(body, false);
        assertStatus(resp, "CREATE WAREHOUSE", 200, 201);

        JsonNode created = locationsClient.parseLocation(resp);
        warehouseId = textOrNull(created, "id");
        warehouseName = textOrNull(created, "name");

        assertNotBlank("warehouseId", warehouseId);
        assertNotBlank("warehouseName", warehouseName);

        return this;
    }

    /**
     * Deletes created warehouse if present.
     *
     * @param reason reason text sent in delete request body
     */
    public void cleanup(String reason) {
        if (!isNotBlank(warehouseId)) return;

        try {
            APIResponse resp = locationsClient.deleteLocation(
                    warehouseId,
                    null,
                    reason == null || reason.isBlank() ? "Delete warehouse via test cleanup" : reason
            );
            assertStatus(resp, "DELETE WAREHOUSE", 200, 204);
        } catch (Exception e) {
            System.out.println("DELETE WAREHOUSE failed: " + e.getMessage());
        }
    }

    public String warehouseId() {
        return warehouseId;
    }

    public String warehouseName() {
        return warehouseName;
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
