package org.example.Api.Transfer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIResponse;
import org.example.Api.helpers.LocationMaterials.LocationMaterialsClient;
import org.example.Api.helpers.LocationsHelper.LocationsClient;
import org.example.Api.helpers.MaterialTransfer.MaterialTransferClient;
import org.example.Api.helpers.MaterialsHelper.MaterialsClient;
import org.example.BaseAPITestExtension.BaseApiTest;
import org.example.apifactories.LocationsTestDataFactory;
import org.example.apifactories.MaterialTransferTestDataFactory;
import org.example.apifactories.MaterialsTestDataFactory;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.Map;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MaterialTransferTest extends BaseApiTest {

    private LocationsClient locationsClient;
    private MaterialsClient materialsClient;
    private MaterialTransferClient transferClient;
    private LocationMaterialsClient locationMaterialsClient;

    private final ObjectMapper om = new ObjectMapper();

    private static String fromLocationId;
    private static String toLocationId;

    private static String materialId;
    private static String materialVariationId;

    // це те, що треба класти в payload dto.id
    private static String materialDetailsLocationId;

    private static final int INITIAL_QTY = 3;
    private static final int TRANSFER_QTY = 1;

    @BeforeAll
    void beforeAll() throws IOException {
        locationsClient = new LocationsClient(userApi);
        materialsClient = new MaterialsClient(userApi);
        transferClient = new MaterialTransferClient(userApi);
        locationMaterialsClient = new LocationMaterialsClient(userApi);

        // 1) CREATE FROM warehouse
        Map<String, Object> fromBody =
                LocationsTestDataFactory.buildCreateWarehouseBody("API FROM Warehouse Transfer E2E ");

        APIResponse fromResp = locationsClient.createLocation(fromBody, false);

        System.out.println("BEFOREALL CREATE FROM WAREHOUSE status: " + fromResp.status());
        System.out.println("BEFOREALL CREATE FROM WAREHOUSE body: " + fromResp.text());
        Assertions.assertTrue(fromResp.status() == 201 || fromResp.status() == 200);

        fromLocationId = locationsClient.extractLocationId(fromResp);
        Assertions.assertNotNull(fromLocationId);
        Assertions.assertFalse(fromLocationId.isEmpty());

        // 2) CREATE material (catalog)
        Map<String, Object> createMatBody = MaterialsTestDataFactory.buildCreateMaterialInLocationRequest(
                "API Transfer Material ",
                "TR-",
                fromLocationId
        );

        APIResponse createMatResp = materialsClient.createMaterial(createMatBody);

        System.out.println("BEFOREALL CREATE MATERIAL status: " + createMatResp.status());
        System.out.println("BEFOREALL CREATE MATERIAL body: " + createMatResp.text());
        Assertions.assertTrue(createMatResp.status() == 200 || createMatResp.status() == 201);

        materialId = materialsClient.extractMaterialId(createMatResp);
        materialVariationId = materialsClient.extractFirstVariationId(createMatResp);

        Assertions.assertNotNull(materialId);
        Assertions.assertFalse(materialId.isEmpty());
        Assertions.assertNotNull(materialVariationId);
        Assertions.assertFalse(materialVariationId.isEmpty());

        // 3) ATTACH material to FROM with INITIAL_QTY
        Map<String, Object> attachBody = MaterialsTestDataFactory.buildAttachMaterialToLocationRequest(
                fromLocationId,
                materialId,
                materialVariationId,
                INITIAL_QTY
        );

        APIResponse attachResp = materialsClient.attachMaterialToLocation(attachBody);

        System.out.println("BEFOREALL ATTACH MATERIAL status: " + attachResp.status());
        System.out.println("BEFOREALL ATTACH MATERIAL body: " + attachResp.text());

        Assertions.assertTrue(
                attachResp.status() == 200 || attachResp.status() == 201 || attachResp.status() == 204,
                "Expected attach to succeed"
        );

        // 4) CREATE TO warehouse
        Map<String, Object> toBody =
                LocationsTestDataFactory.buildCreateWarehouseBody("API TO Warehouse Transfer E2E ");

        APIResponse toResp = locationsClient.createLocation(toBody, false);

        System.out.println("BEFOREALL CREATE TO WAREHOUSE status: " + toResp.status());
        System.out.println("BEFOREALL CREATE TO WAREHOUSE body: " + toResp.text());
        Assertions.assertTrue(toResp.status() == 201 || toResp.status() == 200);

        toLocationId = locationsClient.extractLocationId(toResp);
        Assertions.assertNotNull(toLocationId);
        Assertions.assertFalse(toLocationId.isEmpty());

        // 5) Resolve materialDetailsLocationId (dto.id) з /v2/locations/{from}/materials/search
        // ВАЖЛИВО: беремо саме запис для нашого materialId
        materialDetailsLocationId = findMaterialDetailsLocationIdInLocation(fromLocationId, materialId);

        System.out.println("BEFOREALL materialDetailsLocationId (dto.id): " + materialDetailsLocationId);
        Assertions.assertNotNull(materialDetailsLocationId);
        Assertions.assertFalse(materialDetailsLocationId.isEmpty());

        // sanity qty pre-check from endpoint
        int fromQty = findMaterialQtyInLocation(fromLocationId, materialId);
        System.out.println("BEFOREALL FROM qty: " + fromQty);
        Assertions.assertEquals(INITIAL_QTY, fromQty, "FROM initial qty mismatch (location materials search)");
    }

    @AfterAll
    void afterAll() {
        if (materialId != null && !materialId.isEmpty()) {
            try {
                APIResponse delMat = materialsClient.deleteMaterial(materialId);
                System.out.println("AFTERALL DELETE MATERIAL status: " + delMat.status());
                System.out.println("AFTERALL DELETE MATERIAL body: " + delMat.text());
            } catch (Exception e) {
                System.out.println("AFTERALL DELETE MATERIAL failed: " + e.getMessage());
            }
        }

        if (fromLocationId != null && !fromLocationId.isEmpty()) {
            try {
                APIResponse delFrom = locationsClient.deleteLocation(
                        fromLocationId,
                        null,
                        "Delete FROM warehouse via API E2E cleanup"
                );
                System.out.println("AFTERALL DELETE FROM WAREHOUSE status: " + delFrom.status());
                System.out.println("AFTERALL DELETE FROM WAREHOUSE body: " + delFrom.text());
            } catch (Exception e) {
                System.out.println("AFTERALL DELETE FROM WAREHOUSE failed: " + e.getMessage());
            }
        }

        if (toLocationId != null && !toLocationId.isEmpty()) {
            try {
                APIResponse delTo = locationsClient.deleteLocation(
                        toLocationId,
                        null,
                        "Delete TO warehouse via API E2E cleanup"
                );
                System.out.println("AFTERALL DELETE TO WAREHOUSE status: " + delTo.status());
                System.out.println("AFTERALL DELETE TO WAREHOUSE body: " + delTo.text());
            } catch (Exception e) {
                System.out.println("AFTERALL DELETE TO WAREHOUSE failed: " + e.getMessage());
            }
        }
    }

    @Test
    @Order(1)
    @DisplayName("Transfer Material from location -> location")
    void transferMaterial_usingLocationMaterialsSearchValidation() throws IOException {
        Assertions.assertNotNull(fromLocationId);
        Assertions.assertNotNull(toLocationId);
        Assertions.assertNotNull(materialId);
        Assertions.assertNotNull(materialDetailsLocationId);

        int fromQtyBefore = findMaterialQtyInLocation(fromLocationId, materialId);
        int toQtyBefore = findMaterialQtyInLocation(toLocationId, materialId);

        System.out.println("PRE fromQtyBefore=" + fromQtyBefore + ", toQtyBefore=" + toQtyBefore);

        Assertions.assertTrue(fromQtyBefore >= TRANSFER_QTY, "Not enough qty in FROM location to transfer");

        // PATCH transfering
        Map<String, Object> transferBody = MaterialTransferTestDataFactory.buildTransferRequest(
                toLocationId,
                materialDetailsLocationId,
                TRANSFER_QTY,
                25.5,
                "REPLENISHED"
        );

        System.out.println("TRANSFER REQUEST body: " + transferBody);

        APIResponse transferResp = transferClient.transfer(fromLocationId, transferBody);

        System.out.println("TRANSFER status: " + transferResp.status());
        System.out.println("TRANSFER body: " + transferResp.text());

        Assertions.assertTrue(
                transferResp.status() == 200 || transferResp.status() == 204,
                "Expected transfer to succeed"
        );

        int fromQtyAfter = findMaterialQtyInLocation(fromLocationId, materialId);
        int toQtyAfter = findMaterialQtyInLocation(toLocationId, materialId);

        System.out.println("POST fromQtyAfter=" + fromQtyAfter + ", toQtyAfter=" + toQtyAfter);

        Assertions.assertEquals(fromQtyBefore - TRANSFER_QTY, fromQtyAfter,
                "FROM qty did not decrease correctly (location materials search)");

        Assertions.assertEquals(toQtyBefore + TRANSFER_QTY, toQtyAfter,
                "TO qty did not increase correctly (location materials search)");
    }

    // -------------------------
    // Helpers using your LocationMaterialsClient
    // -------------------------

    private JsonNode fetchLocationMaterialsRoot(String locationId) throws IOException {
        APIResponse resp = locationMaterialsClient.searchMaterialsInLocation(locationId);

        System.out.println("LOCATION MATERIALS SEARCH [" + locationId + "] status: " + resp.status());
        System.out.println("LOCATION MATERIALS SEARCH [" + locationId + "] body: " + resp.text());

        Assertions.assertEquals(200, resp.status(), "Expected 200 from /v2/locations/{id}/materials/search");

        return locationMaterialsClient.parseResponse(resp);
    }

    /**
     * Знаходимо в content[] елемент, який відповідає нашому materialId.
     */
    private JsonNode findMaterialNodeInLocation(String locationId, String materialId) throws IOException {
        JsonNode root = fetchLocationMaterialsRoot(locationId);
        JsonNode content = root.get("content");

        if (content == null || !content.isArray() || content.size() == 0) {
            return null;
        }

        for (JsonNode item : content) {
            // типові варіанти, де може лежати id матеріалу
            String mid = item.path("materialId").asText("");
            if (mid.isEmpty()) mid = item.path("material").path("id").asText("");
            if (mid.isEmpty()) mid = item.path("catalogMaterial").path("id").asText("");
            if (mid.isEmpty()) mid = item.path("materialResponseDto").path("id").asText("");

            if (materialId.equals(mid)) {
                return item;
            }
        }

        return null;
    }

    /**
     * dto.id для transfer — це, як правило, id запису "material in location" (MaterialDetailsLocation).
     * У твоєму клієнті extractMaterialDetailsId() повертає materialNode.get("id").
     * Ми тільки підбираємо правильний node (саме наш materialId).
     */
    private String findMaterialDetailsLocationIdInLocation(String locationId, String materialId) throws IOException {
        JsonNode node = findMaterialNodeInLocation(locationId, materialId);

        Assertions.assertNotNull(node,
                "Material with id=" + materialId + " not found in location materials search for locationId=" + locationId);

        String mdlId = locationMaterialsClient.extractMaterialDetailsId(node);
        Assertions.assertNotNull(mdlId, "Could not extract materialDetailsLocationId (node.id is null)");
        Assertions.assertFalse(mdlId.isEmpty(), "Could not extract materialDetailsLocationId (node.id is empty)");

        return mdlId;
    }

    /**
     * Qty на локації через /v2/locations/{id}/materials/search.
     * Якщо матеріалу ще немає на TO — повертаємо 0.
     */
    private int findMaterialQtyInLocation(String locationId, String materialId) throws IOException {
        JsonNode node = findMaterialNodeInLocation(locationId, materialId);
        if (node == null) return 0;

        return extractQuantity(node);
    }

    /**
     * Універсальний extractor quantity (бо структура може різнитись).
     */
    private int extractQuantity(JsonNode node) {
        // варіант 1: quantity на верхньому рівні
        JsonNode q1 = node.get("quantity");
        if (q1 != null && q1.isNumber()) return q1.asInt();

        // варіант 2: materialDetails.quantity
        JsonNode q2 = node.path("materialDetails").get("quantity");
        if (q2 != null && q2.isNumber()) return q2.asInt();

        // варіант 3: details.quantity
        JsonNode q3 = node.path("details").get("quantity");
        if (q3 != null && q3.isNumber()) return q3.asInt();

        // варіант 4: availableQuantity
        JsonNode q4 = node.get("availableQuantity");
        if (q4 != null && q4.isNumber()) return q4.asInt();

        return 0;
    }
}
