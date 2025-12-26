package org.example.Api.Kits;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.APIResponse;
import org.example.Api.helpers.KitsHelper.KitsClient;
import org.example.Api.helpers.LocationMaterials.LocationMaterialsClient;
import org.example.Api.helpers.MaterialTagsHelper.MaterialTagsClient;
import org.example.Api.helpers.SuppliersContactsHelper.SupplierContactsClient;
import org.example.Api.helpers.ToolsHelper.ToolsClient;
import org.example.BaseAPITestExtension.BaseApiTest;
import org.example.apifactories.KitsTestDataFactory;
import org.example.apifactories.ToolsFinancingTestDataFactory;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class KitsE2ETests extends BaseApiTest {

    private KitsClient kitsClient;
    private LocationMaterialsClient locationMaterialsClient;

    private ToolsClient toolsClient;
    private SupplierContactsClient supplierContactsClient;
    private MaterialTagsClient materialTagsClient;

    private String kitId;
    private String materialVariationId;

    private String financingId;
    private String toolUnitId;

    private String supplierId;
    private String materialTagId;
    private String materialTagName;

    private static final String LOCATION_ID_WAREHOUSE_MAIN =
            "ac1f56fd-9919-137e-8199-1f504b6607e8";

    @BeforeAll
    void initClientsAndResolveDependencies() throws IOException {
        kitsClient = new KitsClient(userApi);
        locationMaterialsClient = new LocationMaterialsClient(userApi);

        toolsClient = new ToolsClient(userApi);
        supplierContactsClient = new SupplierContactsClient(userApi);
        materialTagsClient = new MaterialTagsClient(userApi);

        // 1) materialVariationId
        APIResponse materialsResp = locationMaterialsClient.searchMaterialsInLocation(LOCATION_ID_WAREHOUSE_MAIN);
        Assertions.assertEquals(200, materialsResp.status(), "Expected 200 from materials search");

        JsonNode materialsRoot = locationMaterialsClient.parseResponse(materialsResp);
        JsonNode firstMaterial = locationMaterialsClient.getFirstMaterial(materialsRoot);
        Assertions.assertNotNull(firstMaterial, "No materials found in warehouse, cannot create kit");

        materialVariationId = locationMaterialsClient.extractMaterialVariationId(firstMaterial);
        Assertions.assertNotNull(materialVariationId, "materialVariationId could not be extracted");
        System.out.println("Resolved materialVariationId: " + materialVariationId);

        // 2) supplierId
        APIResponse suppliersResp = supplierContactsClient.getAllSupplierContacts();
        Assertions.assertEquals(200, suppliersResp.status(), "Expected 200 from /supplier-contacts");

        JsonNode suppliersRoot = supplierContactsClient.parseResponse(suppliersResp);
        supplierId = supplierContactsClient.extractFirstSupplierId(suppliersRoot);
        Assertions.assertNotNull(supplierId, "No suppliers found – cannot create toolUnit");
        System.out.println("Resolved supplierId: " + supplierId);

        // 3) materialTagId/name
        APIResponse tagsResp = materialTagsClient.getMaterialTags(0, 20);
        Assertions.assertEquals(200, tagsResp.status(), "Expected 200 from material-tags endpoint");

        JsonNode tagsRoot = materialTagsClient.parseResponse(tagsResp);
        materialTagId = materialTagsClient.extractFirstTagId(tagsRoot);
        materialTagName = materialTagsClient.extractFirstTagName(tagsRoot);
        Assertions.assertNotNull(materialTagId, "No material tags found – cannot create tools financing");
        System.out.println("Resolved materialTagId: " + materialTagId);

        // 4) Create tools financing + embedded toolUnit
        Map<String, Object> toolsFinBody = ToolsFinancingTestDataFactory.buildCreateToolsFinancingBody(
                "API Tools Financing For Kits ",
                "Created to generate toolUnit for kits E2E",
                "API-MFG-KITS-",
                materialTagId,
                materialTagName,
                supplierId,
                LOCATION_ID_WAREHOUSE_MAIN
        );

        APIResponse createToolsResp = toolsClient.createToolsFinancing(toolsFinBody);
        System.out.println("CREATE TOOLS FINANCING status: " + createToolsResp.status());
        System.out.println("CREATE TOOLS FINANCING body: " + createToolsResp.text());

        Assertions.assertEquals(201, createToolsResp.status(), "Expected 201 on tools financing create");

        financingId = toolsClient.extractFinancingId(createToolsResp);
        Assertions.assertNotNull(financingId, "financingId must not be null");

        JsonNode financingRoot = toolsClient.parseFinancing(createToolsResp);
        JsonNode firstToolUnit = toolsClient.getFirstToolUnit(financingRoot);
        Assertions.assertNotNull(firstToolUnit, "Expected toolUnit in tools financing response");

        toolUnitId = firstToolUnit.get("id").asText();
        Assertions.assertNotNull(toolUnitId, "toolUnitId must not be null");
        Assertions.assertFalse(toolUnitId.isEmpty(), "toolUnitId must not be empty");

        System.out.println("Created toolUnitId for kits test: " + toolUnitId);
    }

    @AfterAll
    void cleanup() {
        // kit може вже бути видалений тестом — це ок
        if (kitId != null) {
            try {
                APIResponse r = kitsClient.deleteKits(Collections.singletonList(kitId));
                System.out.println("CLEANUP DELETE KIT status: " + r.status());
            } catch (Exception e) {
                System.out.println("CLEANUP DELETE KIT failed: " + e.getMessage());
            }
        }

        // видаляємо financing (і очікуємо каскадне прибирання toolUnit)
        if (financingId != null) {
            try {
                APIResponse r = toolsClient.deleteToolsFinancing(financingId);
                System.out.println("CLEANUP DELETE TOOLS FINANCING status: " + r.status());
            } catch (Exception e) {
                System.out.println("CLEANUP DELETE TOOLS FINANCING failed: " + e.getMessage());
            }
        }
    }

    @Test
    @Order(1)
    void createKitWithMaterialAndCreatedToolUnit_createsRichKit() throws IOException {
        Map<String, Object> body = KitsTestDataFactory.buildCreateKitBodyWithMaterialToolAndLocation(
                "API Kit (with created toolUnit) ",
                "Created via Kits E2E (material + toolUnit created via tools financing + location)",
                1.0,
                materialVariationId,
                toolUnitId,
                LOCATION_ID_WAREHOUSE_MAIN
        );

        APIResponse response = kitsClient.createKit(body);
        System.out.println("CREATE KIT status: " + response.status());
        System.out.println("CREATE KIT body: " + response.text());

        Assertions.assertEquals(201, response.status(), "Expected 201 on create kit");

        kitId = kitsClient.extractKitId(response);
        Assertions.assertNotNull(kitId, "kitId must not be null after create");

        JsonNode created = kitsClient.parseKit(response);

        Assertions.assertEquals(body.get("name"), created.get("name").asText());
        Assertions.assertEquals(body.get("description"), created.get("description").asText());

        JsonNode materials = created.get("materialDetailsResponseDtos");
        Assertions.assertTrue(materials != null && materials.isArray() && materials.size() > 0);
        JsonNode mv = materials.get(0).get("materialVariationResponseDto");
        Assertions.assertEquals(materialVariationId, mv.get("id").asText());

        JsonNode tools = created.get("toolUnitResponseDtos");
        Assertions.assertTrue(tools != null && tools.isArray() && tools.size() > 0);
        Assertions.assertEquals(toolUnitId, tools.get(0).get("id").asText());
    }

    @Test
    @Order(2)
    void updateKit_updatesMainFieldsButKeepsLinks() throws IOException {
        Assertions.assertNotNull(kitId, "kitId is null – create test probably failed");

        Map<String, Object> body = KitsTestDataFactory.buildUpdateKitBodyWithSameLinks(
                "API Kit UPDATED ",
                "Updated via Kits E2E (links stay the same)",
                2.0,
                materialVariationId,
                toolUnitId,
                LOCATION_ID_WAREHOUSE_MAIN
        );

        APIResponse response = kitsClient.updateKit(kitId, body);
        System.out.println("UPDATE KIT status: " + response.status());
        System.out.println("UPDATE KIT body: " + response.text());

        Assertions.assertTrue(response.status() == 200 || response.status() == 201);

        JsonNode updated = kitsClient.parseKit(response);

        Assertions.assertEquals(kitId, updated.get("id").asText());
        Assertions.assertEquals(body.get("name"), updated.get("name").asText());
        Assertions.assertEquals(body.get("description"), updated.get("description").asText());

        JsonNode tools = updated.get("toolUnitResponseDtos");
        Assertions.assertTrue(tools != null && tools.isArray() && tools.size() > 0);
        Assertions.assertEquals(toolUnitId, tools.get(0).get("id").asText());
    }

    @Test
    @Order(3)
    void deleteKit_deletesById() {
        Assertions.assertNotNull(kitId, "kitId is null – previous tests probably failed");

        APIResponse response = kitsClient.deleteKits(Collections.singletonList(kitId));
        System.out.println("DELETE KIT status: " + response.status());
        System.out.println("DELETE KIT body: '" + response.text() + "'");

        Assertions.assertEquals(204, response.status());

        kitId = null; // щоб cleanup не видаляв вдруге
    }
}
