package org.example.Api.Tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.APIResponse;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.example.Api.helpers.MaterialTagsHelper.MaterialTagsClient;
import org.example.Api.helpers.SuppliersContactsHelper.SupplierContactsClient;
import org.example.Api.helpers.ToolsHelper.ToolsClient;
import org.example.BaseAPITestExtension.BaseApiTest;
import org.example.apifactories.ToolsFinancingTestDataFactory;
import org.example.config.TestEnvironment;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Timeout;

@Epic("Tools")
@Feature("Tools E2E CRUD")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Timeout(value = TestEnvironment.E2E_TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
public class ToolsE2ETests extends BaseApiTest {

    private static final Logger log = LoggerFactory.getLogger(ToolsE2ETests.class);

    private ToolsClient materialsFinancingsToolsClient;
    private SupplierContactsClient supplierContactsClient;
    private MaterialTagsClient materialTagsClient;

    private String financingId;
    private String supplierId;
    private String materialTagId;
    private String materialTagName;
    private String toolUnitId;

    @BeforeAll
    void initClientsAndResolveDependencies() throws IOException {
        materialsFinancingsToolsClient = new ToolsClient(userApi);
        supplierContactsClient = new SupplierContactsClient(userApi);
        materialTagsClient = new MaterialTagsClient(userApi);

        // suppliers
        APIResponse suppliersResp = supplierContactsClient.getAllSupplierContacts();
        int supStatus = suppliersResp.status();

        log.info("SUPPLIER-CONTACTS status: {}", supStatus);
        log.debug("SUPPLIER-CONTACTS body: {}", suppliersResp.text());

        Assertions.assertEquals(200, supStatus, "Expected 200 from GET /supplier-contacts");

        JsonNode suppliersRoot = supplierContactsClient.parseResponse(suppliersResp);
        supplierId = supplierContactsClient.extractFirstSupplierId(suppliersRoot);

        Assertions.assertNotNull(
                supplierId,
                "No suppliers found via /supplier-contacts – please ensure there is at least one supplier"
        );

        log.info("Resolved supplierId from /supplier-contacts: {}", supplierId);

        // material tags
        APIResponse tagsResp = materialTagsClient.getMaterialTags(0, 20);
        int tagsStatus = tagsResp.status();

        log.info("MATERIAL-TAGS status: {}", tagsStatus);
        log.debug("MATERIAL-TAGS body: {}", tagsResp.text());

        Assertions.assertEquals(200, tagsStatus, "Expected 200 from material-tags endpoint");

        JsonNode tagsRoot = materialTagsClient.parseResponse(tagsResp);
        materialTagId = materialTagsClient.extractFirstTagId(tagsRoot);
        materialTagName = materialTagsClient.extractFirstTagName(tagsRoot);

        Assertions.assertNotNull(
                materialTagId,
                "No material tags found – please ensure there is at least one material tag"
        );

        log.info("Resolved materialTagId: {}, materialTagName: {}", materialTagId, materialTagName);
    }

    @DisplayName("Create Tool Financing with embedded tool unit")
    @Test
    @Order(1)
    void createToolsFinancing_createsWithEmbeddedToolUnit() throws IOException {
        Map<String, Object> body = ToolsFinancingTestDataFactory.buildCreateToolsFinancingBody(
                "API Materials Financing Tool ",
                "Created via API E2E materials-financings/tools test",
                "API-MFG-",
                materialTagId,
                materialTagName,
                supplierId,
                TestEnvironment.WAREHOUSE_MAIN_ID
        );

        APIResponse response = materialsFinancingsToolsClient.createToolsFinancing(body);
        int status = response.status();

        log.info("CREATE /materials-financings/tools status: {}", status);
        log.debug("CREATE /materials-financings/tools body: {}", response.text());

        Assertions.assertEquals(201, status, "Expected 201 Created from POST /materials-financings/tools");

        JsonNode root = materialsFinancingsToolsClient.parseFinancing(response);

        financingId = materialsFinancingsToolsClient.extractFinancingId(response);
        Assertions.assertNotNull(financingId, "financing id must not be null");
        Assertions.assertFalse(financingId.isEmpty(), "financing id must not be empty");

        Assertions.assertEquals(body.get("name"), root.get("name").asText(), "name in response must match request");
        Assertions.assertEquals(body.get("description"), root.get("description").asText(), "description in response must match request");
        Assertions.assertEquals(body.get("mfg"), root.get("mfg").asText(), "mfg in response must match request");

        JsonNode tags = root.get("tags");
        Assertions.assertNotNull(tags, "tags should not be null");
        Assertions.assertTrue(tags.isArray(), "tags should be an array");
        Assertions.assertFalse(tags.isEmpty(), "Expected at least one tag");

        JsonNode firstTagResp = tags.get(0);
        Assertions.assertEquals(materialTagId, firstTagResp.get("id").asText(), "tag.id must match materialTagId");
        if (materialTagName != null) {
            Assertions.assertEquals(materialTagName, firstTagResp.get("name").asText(), "tag.name must match materialTagName");
        }

        JsonNode firstToolUnit = materialsFinancingsToolsClient.getFirstToolUnit(root);
        Assertions.assertNotNull(firstToolUnit, "Expected at least one toolUnit in response");
        log.debug("FIRST TOOL UNIT NODE: {}", firstToolUnit.toPrettyString());

        toolUnitId = firstToolUnit.get("id").asText();
        Assertions.assertNotNull(toolUnitId, "toolUnit.id must not be null");
        Assertions.assertFalse(toolUnitId.isEmpty(), "toolUnit.id must not be empty");

        JsonNode locationNode = firstToolUnit.get("location");
        Assertions.assertNotNull(locationNode, "location object not found in firstToolUnit");
        Assertions.assertEquals(
                TestEnvironment.WAREHOUSE_MAIN_ID,
                locationNode.get("id").asText(),
                "location.id must match warehouse location id"
        );

        JsonNode supplierNode = firstToolUnit.get("supplier");
        Assertions.assertNotNull(supplierNode, "supplier object not found in firstToolUnit");
        Assertions.assertEquals(
                supplierId,
                supplierNode.get("id").asText(),
                "supplier.id must match supplierId from /supplier-contacts"
        );

        Assertions.assertEquals("AVAILABLE", firstToolUnit.get("status").asText(), "toolUnit.status must be AVAILABLE");

        Assertions.assertTrue(
                firstToolUnit.hasNonNull("purchaseDate") && !firstToolUnit.get("purchaseDate").asText().isEmpty(),
                "purchaseDate should not be null or empty"
        );
    }

    @DisplayName("Update Tool Financing - update fields, keep links")
    @Test
    @Order(2)
    void updateToolsFinancing_updatesMainFieldsButKeepsLinks() throws IOException {
        Assertions.assertNotNull(financingId, "financingId is null – create test probably failed");
        Assertions.assertNotNull(toolUnitId, "toolUnitId is null – create test probably failed");

        Map<String, Object> body = ToolsFinancingTestDataFactory.buildUpdateToolsFinancingBody(
                financingId,
                toolUnitId,
                "API Materials Financing Tool UPDATED ",
                "Updated via API E2E materials-financings/tools test",
                "API-MFG-UPDATED-",
                materialTagId,
                materialTagName,
                supplierId,
                TestEnvironment.WAREHOUSE_MAIN_ID
        );

        APIResponse response = materialsFinancingsToolsClient.updateToolsFinancing(financingId, body);
        int status = response.status();

        log.info("UPDATE /materials-financings/tools status: {}", status);
        log.debug("UPDATE /materials-financings/tools body: {}", response.text());

        Assertions.assertEquals(200, status, "Expected 200 OK from PUT /materials-financings/tools/{id}");

        JsonNode root = materialsFinancingsToolsClient.parseFinancing(response);

        Assertions.assertEquals(financingId, root.get("id").asText(), "Updated financing id must remain the same");

        Assertions.assertEquals(body.get("name"), root.get("name").asText(), "Updated name must match request");
        Assertions.assertEquals(body.get("description"), root.get("description").asText(), "Updated description must match request");
        Assertions.assertEquals(body.get("mfg"), root.get("mfg").asText(), "Updated mfg must match request");

        JsonNode tags = root.get("tags");
        Assertions.assertNotNull(tags, "tags should not be null after update");
        Assertions.assertTrue(tags.isArray(), "tags should be an array after update");
        Assertions.assertFalse(tags.isEmpty(), "Expected at least one tag after update");
        Assertions.assertEquals(materialTagId, tags.get(0).get("id").asText(), "tag.id after update must still match");

        JsonNode firstToolUnit = materialsFinancingsToolsClient.getFirstToolUnit(root);
        Assertions.assertNotNull(firstToolUnit, "Expected at least one toolUnit in response after update");
        log.debug("UPDATED FIRST TOOL UNIT NODE: {}", firstToolUnit.toPrettyString());

        Assertions.assertEquals(toolUnitId, firstToolUnit.get("id").asText(), "toolUnit.id after update must remain the same");

        JsonNode locationNode = firstToolUnit.get("location");
        Assertions.assertNotNull(locationNode, "location object not found in updated firstToolUnit");
        Assertions.assertEquals(TestEnvironment.WAREHOUSE_MAIN_ID, locationNode.get("id").asText(), "location.id must remain same");

        JsonNode supplierNode = firstToolUnit.get("supplier");
        Assertions.assertNotNull(supplierNode, "supplier object not found in updated firstToolUnit");
        Assertions.assertEquals(supplierId, supplierNode.get("id").asText(), "supplier.id must remain same");
    }

    @DisplayName("Delete Tool Financing by ID")
    @Test
    @Order(3)
    void deleteToolsFinancing_deletesById() {
        Assertions.assertNotNull(financingId, "financingId is null – previous tests probably failed");

        APIResponse response = materialsFinancingsToolsClient.deleteToolsFinancing(financingId);
        int status = response.status();

        log.info("DELETE /materials-financings/tools/{id} status: {}", status);
        log.debug("DELETE /materials-financings/tools/{id} body: '{}'", response.text());

        Assertions.assertEquals(204, status, "Expected 204 No Content from DELETE /materials-financings/tools/{id}");
    }
}
