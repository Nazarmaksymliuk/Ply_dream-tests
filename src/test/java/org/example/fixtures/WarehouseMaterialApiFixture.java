package org.example.fixtures;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import org.example.Api.helpers.LocationsHelper.LocationsClient;
import org.example.Api.helpers.MaterialsHelper.MaterialsClient;
import org.example.apifactories.LocationsTestDataFactory;
import org.example.apifactories.MaterialsTestDataFactory;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.util.Map;

/**
 * Reusable fixture: creates a warehouse, creates a catalog material,
 * attaches the material variation to the warehouse, and optionally verifies the attachment.
 *
 * Intended to be used across multiple UI/API tests.
 */
public class WarehouseMaterialApiFixture {

    private final LocationsClient locationsClient;
    private final MaterialsClient materialsClient;
    private final ObjectMapper om = new ObjectMapper();

    private String warehouseId;
    private String warehouseName;
    private String materialId;
    private String materialVariationId;

    private WarehouseMaterialApiFixture(LocationsClient locationsClient, MaterialsClient materialsClient) {
        this.locationsClient = locationsClient;
        this.materialsClient = materialsClient;
    }

    public static WarehouseMaterialApiFixture create(APIRequestContext requestContext) {
        return new WarehouseMaterialApiFixture(
                new LocationsClient(requestContext),
                new MaterialsClient(requestContext)
        );
    }

    /**
     * High-level scenario: warehouse + material + attach + verify.
     */
    public WarehouseMaterialApiFixture provisionWarehouseWithAttachedMaterial() throws IOException {
        createWarehouse();
        createMaterial();
        attachMaterialToWarehouse();
        verifyMaterialAttachedToWarehouse(); // можна прибрати, якщо інколи не треба
        return this;
    }

    // -------------------------
    // Steps
    // -------------------------

    private void createWarehouse() throws IOException {
        Map<String, Object> warehouseBody =
                LocationsTestDataFactory.buildCreateWarehouseBody("UI Warehouse ");

        APIResponse whResponse = locationsClient.createLocation(warehouseBody, false);

        assertStatus(whResponse, "CREATE WAREHOUSE", 200, 201);

        JsonNode createdWarehouse = locationsClient.parseLocation(whResponse);
        warehouseId = textOrNull(createdWarehouse, "id");
        warehouseName = textOrNull(createdWarehouse, "name");

        assertNotBlank("warehouseId", warehouseId);
        assertNotBlank("warehouseName", warehouseName);
    }

    private void createMaterial() throws IOException {
        // фабрика у тебе приймає locationId (хоч він "не критичний") — лишаємо як є
        Map<String, Object> createMaterialBody =
                MaterialsTestDataFactory.buildCreateMaterialInLocationRequest(
                        "UI Material ",
                        "UI-",
                        warehouseId
                );

        APIResponse createMatResp = materialsClient.createMaterial(createMaterialBody);

        assertStatus(createMatResp, "CREATE MATERIAL", 200, 201);

        materialId = materialsClient.extractMaterialId(createMatResp);
        materialVariationId = materialsClient.extractFirstVariationId(createMatResp);

        assertNotBlank("materialId", materialId);
        assertNotBlank("materialVariationId", materialVariationId);
    }

    private void attachMaterialToWarehouse() throws IOException {
        Map<String, Object> attachBody =
                MaterialsTestDataFactory.buildAttachMaterialToLocationRequest(
                        warehouseId,
                        materialId,
                        materialVariationId,
                        1
                );

        APIResponse attachResp = materialsClient.attachMaterialToLocation(attachBody);

        assertStatus(attachResp, "ATTACH MATERIAL", 200, 201, 204);
    }

    /**
     * Verifies that created material has locationDetails linking to created warehouse.
     * Uses GET /materials-financings/materials/{id}.
     */
    private void verifyMaterialAttachedToWarehouse() throws IOException {
        APIResponse getResp = materialsClient.getMaterial(materialId);
        assertStatus(getResp, "GET MATERIAL", 200);

        JsonNode root = om.readTree(getResp.text());

        JsonNode mdl = root.path("materialVariations")
                .path(0)
                .path("materialDetailsWithLocations");

        Assertions.assertTrue(
                mdl.isArray() && mdl.size() > 0,
                "Material created but NOT attached to location (materialDetailsWithLocations empty)"
        );

        String attachedLocationId = mdl.get(0).path("location").path("id").asText(null);
        Assertions.assertEquals(
                warehouseId,
                attachedLocationId,
                "Material attached to wrong location"
        );
    }

    // -------------------------
    // Cleanup (safe)
    // -------------------------

    public void cleanup() {
        // Material first (часто блокує видалення локації)
        safe("DELETE MATERIAL", () -> {
            if (isNotBlank(materialId)) {
                APIResponse delMat = materialsClient.deleteMaterial(materialId);
                assertStatus(delMat, "DELETE MATERIAL", 200, 204);
            }
        });

        safe("DELETE WAREHOUSE", () -> {
            if (isNotBlank(warehouseId)) {
                APIResponse delLoc = locationsClient.deleteLocation(
                        warehouseId,
                        null,
                        "Delete warehouse via test cleanup"
                );
                assertStatus(delLoc, "DELETE WAREHOUSE", 200, 204);
            }
        });
    }

    // -------------------------
    // Getters
    // -------------------------

    public String warehouseId() {
        return warehouseId;
    }

    public String warehouseName() {
        return warehouseName;
    }

    public String materialId() {
        return materialId;
    }

    public String materialVariationId() {
        return materialVariationId;
    }

    // -------------------------
    // Small helpers
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

    private static void safe(String label, ThrowingRunnable r) {
        try {
            r.run();
        } catch (Exception e) {
            System.out.println(label + " failed: " + e.getMessage());
        }
    }


    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
