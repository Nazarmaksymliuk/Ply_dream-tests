package org.example.Api.Kits;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.APIResponse;
import org.example.Api.helpers.KitsHelper.KitsClient;
import org.example.Api.helpers.LocationMaterials.LocationMaterialsClient;
import org.example.Api.helpers.ToolUnits.ToolUnitsClient;
import org.example.BaseAPITestExtension.BaseApiTest;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class KitsE2ETests extends BaseApiTest {

    private KitsClient kitsClient;
    private LocationMaterialsClient locationMaterialsClient;
    private ToolUnitsClient toolUnitsClient;

    private String kitId;
    private String materialVariationId;
    private String toolUnitId;

    // стабільний склад (WarehouseMain)
    private static final String LOCATION_ID_WAREHOUSE_MAIN = "ac1f56fd-9919-137e-8199-1f504b6607e8";

    @BeforeAll
    void initClientsAndResolveDependencies() throws IOException {
        kitsClient = new KitsClient(userApi);
        locationMaterialsClient = new LocationMaterialsClient(userApi);
        toolUnitsClient = new ToolUnitsClient(userApi);

        // 1️⃣ тягнемо матеріали в локації
        APIResponse materialsResp =
                locationMaterialsClient.searchMaterialsInLocation(LOCATION_ID_WAREHOUSE_MAIN);
        int matStatus = materialsResp.status();

        System.out.println("MATERIALS SEARCH status: " + matStatus);
        System.out.println("MATERIALS SEARCH body: " + materialsResp.text());

        Assertions.assertEquals(
                200,
                matStatus,
                "Expected 200 from /v2/locations/{id}/materials/search"
        );

        JsonNode materialsRoot = locationMaterialsClient.parseResponse(materialsResp);
        JsonNode firstMaterial = locationMaterialsClient.getFirstMaterial(materialsRoot);

        Assertions.assertNotNull(
                firstMaterial,
                "No materials found in location " + LOCATION_ID_WAREHOUSE_MAIN
                        + " – please ensure there is at least one material in this warehouse"
        );

        materialVariationId = locationMaterialsClient.extractMaterialVariationId(firstMaterial);
        Assertions.assertNotNull(
                materialVariationId,
                "materialVariationId could not be extracted from materials search result"
        );

        System.out.println("Resolved materialVariationId from search: " + materialVariationId);

        // 2️⃣ тягнемо tool units по тому ж складу
        APIResponse tuResponse = toolUnitsClient.searchToolUnitsByLocation(LOCATION_ID_WAREHOUSE_MAIN);
        int tuStatus = tuResponse.status();

        System.out.println("TOOL UNITS status: " + tuStatus);
        System.out.println("TOOL UNITS body: " + tuResponse.text());

        Assertions.assertEquals(200, tuStatus, "Expected 200 from tool-units search");

        JsonNode tuRoot = toolUnitsClient.parseResponse(tuResponse);
        JsonNode firstToolUnit = toolUnitsClient.getFirstToolUnit(tuRoot);

        Assertions.assertNotNull(
                firstToolUnit,
                "No tool units found in location " + LOCATION_ID_WAREHOUSE_MAIN
                        + " – please ensure there is at least one tool unit in this warehouse"
        );

        toolUnitId = toolUnitsClient.extractToolUnitId(firstToolUnit);
        Assertions.assertNotNull(
                toolUnitId,
                "toolUnitId could not be extracted from tool unit search result"
        );

        System.out.println("Resolved toolUnitId from search: " + toolUnitId);
    }

    // 1️⃣ CREATE: POST /kits/create – кіт з матеріалом + тулом + локацією
    @Test
    @Order(1)
    void createKitWithMaterialAndTool_createsRichKit() throws IOException {
        Map<String, Object> body = buildCreateKitBodyWithMaterialAndTool();

        APIResponse response = kitsClient.createKit(body);
        int status = response.status();

        System.out.println("CREATE KIT status: " + status);
        System.out.println("CREATE KIT body: " + response.text());

        Assertions.assertEquals(
                201,
                status,
                "Expected 201 on create kit, but got: " + status
        );

        kitId = kitsClient.extractKitId(response);
        Assertions.assertNotNull(kitId, "kitId must not be null after create");
        Assertions.assertFalse(kitId.isEmpty(), "kitId must not be empty");

        JsonNode created = kitsClient.parseKit(response);

        // базові поля
        Assertions.assertEquals(
                body.get("name"),
                created.get("name").asText(),
                "Created name must match request"
        );
        Assertions.assertEquals(
                ((Number) body.get("cost")).doubleValue(),
                created.get("cost").asDouble(),
                0.0001,
                "Created cost must match request"
        );
        Assertions.assertEquals(
                body.get("description"),
                created.get("description").asText(),
                "Created description must match request"
        );

        // 🔍 матеріал в кітi
        JsonNode materials = created.get("materialDetailsResponseDtos");
        Assertions.assertNotNull(materials, "materialDetailsResponseDtos should not be null");
        Assertions.assertTrue(
                materials.isArray() && materials.size() > 0,
                "Expected at least one material in kit"
        );

        JsonNode firstMat = materials.get(0);
        JsonNode mv = firstMat.get("materialVariationResponseDto");
        Assertions.assertNotNull(mv, "materialVariationResponseDto should not be null");
        Assertions.assertEquals(
                materialVariationId,
                mv.get("id").asText(),
                "materialVariation id in kit should match materialVariationId from search"
        );

        // 🔍 тул-юнiт
        JsonNode tools = created.get("toolUnitResponseDtos");
        Assertions.assertNotNull(tools, "toolUnitResponseDtos should not be null");
        Assertions.assertTrue(
                tools.isArray() && tools.size() > 0,
                "Expected at least one tool unit in kit"
        );
        Assertions.assertEquals(
                toolUnitId,
                tools.get(0).get("id").asText(),
                "toolUnit id in kit should match toolUnitId from search"
        );

        // 🔍 локація
        JsonNode locations = created.get("locationResponseDtos");
        Assertions.assertNotNull(locations, "locationResponseDtos should not be null");
        Assertions.assertTrue(
                locations.isArray() && locations.size() > 0,
                "Expected at least one location in kit"
        );
        Assertions.assertEquals(
                LOCATION_ID_WAREHOUSE_MAIN,
                locations.get(0).get("id").asText(),
                "location id in kit should match warehouse location"
        );
    }

    // 2️⃣ UPDATE: PUT /kits/{kitId}/update
    @Test
    @Order(2)
    void updateKit_updatesMainFieldsButKeepsLinks() throws IOException {
        Assertions.assertNotNull(kitId, "kitId is null – create test probably failed");

        Map<String, Object> body = buildUpdateKitBodyWithSameLinks();

        APIResponse response = kitsClient.updateKit(kitId, body);
        int status = response.status();

        System.out.println("UPDATE KIT status: " + status);
        System.out.println("UPDATE KIT body: " + response.text());

        Assertions.assertTrue(
                status == 200 || status == 201,
                "Expected 200 or 201 on update kit, but got: " + status
        );

        JsonNode updated = kitsClient.parseKit(response);

        Assertions.assertEquals(
                kitId,
                updated.get("id").asText(),
                "Updated id must be the same as created"
        );

        Assertions.assertEquals(
                body.get("name"),
                updated.get("name").asText(),
                "Updated name must match request"
        );
        Assertions.assertEquals(
                body.get("description"),
                updated.get("description").asText(),
                "Updated description must match request"
        );
        Assertions.assertEquals(
                ((Number) body.get("cost")).doubleValue(),
                updated.get("cost").asDouble(),
                0.0001,
                "Updated cost must match request"
        );

        // перевіряємо, що лінки залишились
        JsonNode materials = updated.get("materialDetailsResponseDtos");
        Assertions.assertNotNull(materials, "materialDetailsResponseDtos should not be null after update");
        Assertions.assertTrue(
                materials.isArray() && materials.size() > 0,
                "Expected at least one material in kit after update"
        );
        JsonNode mv = materials.get(0).get("materialVariationResponseDto");
        Assertions.assertEquals(
                materialVariationId,
                mv.get("id").asText(),
                "materialVariation id in updated kit should remain the same"
        );

        JsonNode tools = updated.get("toolUnitResponseDtos");
        Assertions.assertNotNull(tools, "toolUnitResponseDtos should not be null after update");
        Assertions.assertTrue(
                tools.isArray() && tools.size() > 0,
                "Expected at least one tool unit in kit after update"
        );
        Assertions.assertEquals(
                toolUnitId,
                tools.get(0).get("id").asText(),
                "toolUnit id in updated kit should remain the same"
        );
    }

    // 3️⃣ DELETE: DELETE /kits/delete
    @Test
    @Order(3)
    void deleteKit_deletesById() {
        Assertions.assertNotNull(kitId, "kitId is null – previous tests probably failed");

        APIResponse response = kitsClient.deleteKits(Collections.singletonList(kitId));
        int status = response.status();

        System.out.println("DELETE KIT status: " + status);
        System.out.println("DELETE KIT body: '" + response.text() + "'");

        Assertions.assertEquals(
                204,
                status,
                "Expected 204 No Content on delete kit, but got: " + status
        );
    }

    // ---------- helpers ----------

    private Map<String, Object> buildCreateKitBodyWithMaterialAndTool() {
        Map<String, Object> body = new HashMap<>();
        long ts = System.currentTimeMillis();

        body.put("name", "API Kit with Material & Tool " + ts);
        body.put("description", "Created via API E2E kit test with material + tool + location");
        body.put("cost", 1.0);

        body.put("barcodes", Collections.emptyList());

        Map<String, Object> kitMaterial = new HashMap<>();
        kitMaterial.put("materialVariationId", materialVariationId);
        kitMaterial.put("quantity", 1);
        kitMaterial.put("cost", 1.0);
        body.put("kitMaterialVariationRequestDtos", List.of(kitMaterial));

        body.put("toolUnitIds", List.of(toolUnitId));

        body.put("locationIds", List.of(LOCATION_ID_WAREHOUSE_MAIN));

        Map<String, Object> locationWithPos = new HashMap<>();
        locationWithPos.put("locationId", LOCATION_ID_WAREHOUSE_MAIN);

        Map<String, Object> position = new HashMap<>();
        position.put("aisle", null);
        position.put("bay", null);
        position.put("bin", null);
        position.put("level", null);
        locationWithPos.put("locationPosition", position);

        body.put("locationIdsWithPositions", List.of(locationWithPos));
        body.put("kitTagRequestDtos", Collections.emptyList());

        return body;
    }

    private Map<String, Object> buildUpdateKitBodyWithSameLinks() {
        Map<String, Object> body = new HashMap<>();
        long ts = System.currentTimeMillis();

        body.put("name", "API Kit UPDATED " + ts);
        body.put("description", "Updated via API E2E kit test (material + tool + location stay the same)");
        body.put("cost", 2.0);

        body.put("barcodes", Collections.emptyList());

        Map<String, Object> kitMaterial = new HashMap<>();
        kitMaterial.put("materialVariationId", materialVariationId);
        kitMaterial.put("quantity", 1);
        kitMaterial.put("cost", 2.0);
        body.put("kitMaterialVariationRequestDtos", List.of(kitMaterial));

        body.put("toolUnitIds", List.of(toolUnitId));

        body.put("locationIds", List.of(LOCATION_ID_WAREHOUSE_MAIN));

        Map<String, Object> locationWithPos = new HashMap<>();
        locationWithPos.put("locationId", LOCATION_ID_WAREHOUSE_MAIN);

        Map<String, Object> position = new HashMap<>();
        position.put("aisle", null);
        position.put("bay", null);
        position.put("bin", null);
        position.put("level", null);
        locationWithPos.put("locationPosition", position);

        body.put("locationIdsWithPositions", List.of(locationWithPos));
        body.put("kitTagRequestDtos", Collections.emptyList());

        return body;
    }
}
