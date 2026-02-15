package org.example.Api.Kits;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.APIResponse;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.example.Api.helpers.KitsHelper.KitsClient;
import org.example.Api.helpers.LocationMaterials.LocationMaterialsClient;
import org.example.Api.helpers.MaterialTagsHelper.MaterialTagsClient;
import org.example.Api.helpers.MaterialsHelper.MaterialsClient;
import org.example.Api.helpers.SuppliersContactsHelper.SupplierContactsClient;
import org.example.Api.helpers.ToolsHelper.ToolsClient;
import org.example.BaseAPITestExtension.BaseApiTest;
import org.example.apifactories.KitsTestDataFactory;
import org.example.apifactories.MaterialsTestDataFactory;
import org.example.apifactories.ToolsFinancingTestDataFactory;
import org.example.config.TestEnvironment;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Timeout;

@Epic("Kits")
@Feature("Kits E2E CRUD")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Timeout(value = TestEnvironment.E2E_TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
public class KitsE2ETests extends BaseApiTest {

    private static final Logger log = LoggerFactory.getLogger(KitsE2ETests.class);

    private KitsClient kitsClient;
    private LocationMaterialsClient locationMaterialsClient;
    private MaterialsClient materialsClient;

    private ToolsClient toolsClient;
    private SupplierContactsClient supplierContactsClient;
    private MaterialTagsClient materialTagsClient;

    private String kitId;
    private String materialVariationId;
    private String createdMaterialId;

    private String financingId;
    private String toolUnitId;

    private String supplierId;
    private String materialTagId;
    private String materialTagName;

    @BeforeAll
    void initClientsAndResolveDependencies() throws IOException {
        kitsClient = new KitsClient(userApi);
        locationMaterialsClient = new LocationMaterialsClient(userApi);
        materialsClient = new MaterialsClient(userApi);

        toolsClient = new ToolsClient(userApi);
        supplierContactsClient = new SupplierContactsClient(userApi);
        materialTagsClient = new MaterialTagsClient(userApi);

        // 1) Create material in location (to guarantee it exists)
        Map<String, Object> materialBody = MaterialsTestDataFactory.buildCreateMaterialInLocationRequest(
                "API Material For Kits ",
                "MAT-KITS-",
                TestEnvironment.WAREHOUSE_MAIN_ID
        );

        APIResponse createMatResp = materialsClient.createMaterial(materialBody);
        log.info("CREATE MATERIAL status: {}", createMatResp.status());
        log.debug("CREATE MATERIAL body: {}", createMatResp.text());
        Assertions.assertEquals(201, createMatResp.status(), "Expected 201 on material create");

        createdMaterialId = materialsClient.extractMaterialId(createMatResp);
        Assertions.assertNotNull(createdMaterialId, "createdMaterialId must not be null");
        log.info("Created material for Kits: {}", createdMaterialId);

        // Extract materialVariationId from create response
        materialVariationId = materialsClient.extractFirstVariationId(createMatResp);
        Assertions.assertNotNull(materialVariationId, "materialVariationId could not be extracted from create response");
        log.info("Resolved materialVariationId: {}", materialVariationId);

        // 2) supplierId
        APIResponse suppliersResp = supplierContactsClient.getAllSupplierContacts();
        Assertions.assertEquals(200, suppliersResp.status(), "Expected 200 from /supplier-contacts");

        JsonNode suppliersRoot = supplierContactsClient.parseResponse(suppliersResp);
        supplierId = supplierContactsClient.extractFirstSupplierId(suppliersRoot);
        Assertions.assertNotNull(supplierId, "No suppliers found – cannot create toolUnit");
        log.info("Resolved supplierId: {}", supplierId);

        // 3) materialTagId/name
        APIResponse tagsResp = materialTagsClient.getMaterialTags(0, 20);
        Assertions.assertEquals(200, tagsResp.status(), "Expected 200 from material-tags endpoint");

        JsonNode tagsRoot = materialTagsClient.parseResponse(tagsResp);
        materialTagId = materialTagsClient.extractFirstTagId(tagsRoot);
        materialTagName = materialTagsClient.extractFirstTagName(tagsRoot);
        Assertions.assertNotNull(materialTagId, "No material tags found – cannot create tools financing");
        log.info("Resolved materialTagId: {}", materialTagId);

        // 4) Create tools financing + embedded toolUnit
        Map<String, Object> toolsFinBody = ToolsFinancingTestDataFactory.buildCreateToolsFinancingBody(
                "API Tools Financing For Kits ",
                "Created to generate toolUnit for kits E2E",
                "API-MFG-KITS-",
                materialTagId,
                materialTagName,
                supplierId,
                TestEnvironment.WAREHOUSE_MAIN_ID
        );

        APIResponse createToolsResp = toolsClient.createToolsFinancing(toolsFinBody);
        log.info("CREATE TOOLS FINANCING status: {}", createToolsResp.status());
        log.debug("CREATE TOOLS FINANCING body: {}", createToolsResp.text());

        Assertions.assertEquals(201, createToolsResp.status(), "Expected 201 on tools financing create");

        financingId = toolsClient.extractFinancingId(createToolsResp);
        Assertions.assertNotNull(financingId, "financingId must not be null");

        JsonNode financingRoot = toolsClient.parseFinancing(createToolsResp);
        JsonNode firstToolUnit = toolsClient.getFirstToolUnit(financingRoot);
        Assertions.assertNotNull(firstToolUnit, "Expected toolUnit in tools financing response");

        toolUnitId = firstToolUnit.get("id").asText();
        Assertions.assertNotNull(toolUnitId, "toolUnitId must not be null");
        Assertions.assertFalse(toolUnitId.isEmpty(), "toolUnitId must not be empty");

        log.info("Created toolUnitId for kits test: {}", toolUnitId);
    }

    @AfterAll
    void cleanup() {
        if (kitId != null) {
            try {
                APIResponse r = kitsClient.deleteKits(Collections.singletonList(kitId));
                log.info("CLEANUP DELETE KIT status: {}", r.status());
            } catch (Exception e) {
                log.warn("CLEANUP DELETE KIT failed: {}", e.getMessage());
            }
        }

        if (financingId != null) {
            try {
                APIResponse r = toolsClient.deleteToolsFinancing(financingId);
                log.info("CLEANUP DELETE TOOLS FINANCING status: {}", r.status());
            } catch (Exception e) {
                log.warn("CLEANUP DELETE TOOLS FINANCING failed: {}", e.getMessage());
            }
        }

        if (createdMaterialId != null) {
            try {
                APIResponse r = materialsClient.deleteMaterial(createdMaterialId);
                log.info("CLEANUP DELETE MATERIAL status: {}", r.status());
            } catch (Exception e) {
                log.warn("CLEANUP DELETE MATERIAL failed: {}", e.getMessage());
            }
        }
    }

    @DisplayName("Create Kit with material and tool unit")
    @Test
    @Order(1)
    void createKitWithMaterialAndCreatedToolUnit_createsRichKit() throws IOException {
        Map<String, Object> body = KitsTestDataFactory.buildCreateKitBodyWithMaterialToolAndLocation(
                "API Kit (with created toolUnit) ",
                "Created via Kits E2E (material + toolUnit created via tools financing + location)",
                1.0,
                materialVariationId,
                toolUnitId,
                TestEnvironment.WAREHOUSE_MAIN_ID
        );

        APIResponse response = kitsClient.createKit(body);
        log.info("CREATE KIT status: {}", response.status());
        log.debug("CREATE KIT body: {}", response.text());

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

    @DisplayName("Update Kit - update fields, keep links")
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
                TestEnvironment.WAREHOUSE_MAIN_ID
        );

        APIResponse response = kitsClient.updateKit(kitId, body);
        log.info("UPDATE KIT status: {}", response.status());
        log.debug("UPDATE KIT body: {}", response.text());

        Assertions.assertTrue(response.status() == 200 || response.status() == 201);

        JsonNode updated = kitsClient.parseKit(response);

        Assertions.assertEquals(kitId, updated.get("id").asText());
        Assertions.assertEquals(body.get("name"), updated.get("name").asText());
        Assertions.assertEquals(body.get("description"), updated.get("description").asText());

        JsonNode tools = updated.get("toolUnitResponseDtos");
        Assertions.assertTrue(tools != null && tools.isArray() && tools.size() > 0);
        Assertions.assertEquals(toolUnitId, tools.get(0).get("id").asText());
    }

    @DisplayName("Delete Kit by ID")
    @Test
    @Order(3)
    void deleteKit_deletesById() {
        Assertions.assertNotNull(kitId, "kitId is null – previous tests probably failed");

        APIResponse response = kitsClient.deleteKits(Collections.singletonList(kitId));
        log.info("DELETE KIT status: {}", response.status());
        log.debug("DELETE KIT body: '{}'", response.text());

        Assertions.assertEquals(204, response.status());

        kitId = null;
    }
}
