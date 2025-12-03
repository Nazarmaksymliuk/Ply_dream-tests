package org.example.Api.Tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.APIResponse;
import org.example.Api.helpers.MaterialTagsHelper.MaterialTagsClient;
import org.example.Api.helpers.SuppliersContactsHelper.SupplierContactsClient;
import org.example.Api.helpers.ToolsHelper.ToolsClient;
import org.example.BaseAPITestExtension.BaseApiTest;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ToolsE2ETests extends BaseApiTest {

    private ToolsClient materialsFinancingsToolsClient;
    private SupplierContactsClient supplierContactsClient;
    private MaterialTagsClient materialTagsClient;

    private String financingId;
    private String supplierId;
    private String materialTagId;
    private String materialTagName;
    private String toolUnitId;   // id створеного toolUnit для апдейта

    private static final String LOCATION_ID_WAREHOUSE_MAIN =
            "ac1f56fd-9919-137e-8199-1f504b6607e8";

    @BeforeAll
    void initClientsAndResolveDependencies() throws IOException {
        materialsFinancingsToolsClient = new ToolsClient(userApi);
        supplierContactsClient = new SupplierContactsClient(userApi);
        materialTagsClient = new MaterialTagsClient(userApi);

        // Суплаєри
        APIResponse suppliersResp = supplierContactsClient.getAllSupplierContacts();
        int supStatus = suppliersResp.status();

        System.out.println("SUPPLIER-CONTACTS status: " + supStatus);
        System.out.println("SUPPLIER-CONTACTS body: " + suppliersResp.text());

        Assertions.assertEquals(
                200,
                supStatus,
                "Expected 200 from GET /supplier-contacts"
        );

        JsonNode suppliersRoot = supplierContactsClient.parseResponse(suppliersResp);
        supplierId = supplierContactsClient.extractFirstSupplierId(suppliersRoot);

        Assertions.assertNotNull(
                supplierId,
                "No suppliers found via /supplier-contacts – please ensure there is at least one supplier"
        );

        System.out.println("Resolved supplierId from /supplier-contacts: " + supplierId);

        //  Material tags
        APIResponse tagsResp = materialTagsClient.getMaterialTags(0, 20);
        int tagsStatus = tagsResp.status();

        System.out.println("MATERIAL-TAGS status: " + tagsStatus);
        System.out.println("MATERIAL-TAGS body: " + tagsResp.text());

        Assertions.assertEquals(
                200,
                tagsStatus,
                "Expected 200 from GET /materials-financings/materials/material-tags"
        );

        JsonNode tagsRoot = materialTagsClient.parseResponse(tagsResp);
        materialTagId = materialTagsClient.extractFirstTagId(tagsRoot);
        materialTagName = materialTagsClient.extractFirstTagName(tagsRoot);

        Assertions.assertNotNull(
                materialTagId,
                "No material tags found – please ensure there is at least one material tag"
        );

        System.out.println("Resolved materialTagId from material-tags: " + materialTagId);
        System.out.println("Resolved materialTagName from material-tags: " + materialTagName);
    }

    // 1️⃣ CREATE: POST /materials-financings/tools
    @Test
    @Order(1)
    void createToolsFinancing_createsWithEmbeddedToolUnit() throws IOException {
        Map<String, Object> body = buildCreateToolsFinancingBody();

        APIResponse response = materialsFinancingsToolsClient.createToolsFinancing(body);
        int status = response.status();

        System.out.println("CREATE /materials-financings/tools status: " + status);
        System.out.println("CREATE /materials-financings/tools body: " + response.text());

        Assertions.assertEquals(
                201,
                status,
                "Expected 201 Created from POST /materials-financings/tools, but got: " + status
        );

        JsonNode root = materialsFinancingsToolsClient.parseFinancing(response);

        // id створеного фінансування
        financingId = materialsFinancingsToolsClient.extractFinancingId(response);
        Assertions.assertNotNull(financingId, "financing id must not be null");
        Assertions.assertFalse(financingId.isEmpty(), "financing id must not be empty");

        // базові поля
        Assertions.assertEquals(
                body.get("name"),
                root.get("name").asText(),
                "name in response must match request"
        );
        Assertions.assertEquals(
                body.get("description"),
                root.get("description").asText(),
                "description in response must match request"
        );
        Assertions.assertEquals(
                body.get("mfg"),
                root.get("mfg").asText(),
                "mfg in response must match request"
        );

        // 🔍 tags
        JsonNode tags = root.get("tags");
        Assertions.assertNotNull(tags, "tags should not be null");
        Assertions.assertTrue(tags.isArray(), "tags should be an array");
        Assertions.assertFalse(tags.isEmpty(), "Expected at least one tag");

        JsonNode firstTagResp = tags.get(0);
        Assertions.assertEquals(
                materialTagId,
                firstTagResp.get("id").asText(),
                "tag.id must match materialTagId from material-tags endpoint"
        );
        if (materialTagName != null) {
            Assertions.assertEquals(
                    materialTagName,
                    firstTagResp.get("name").asText(),
                    "tag.name must match materialTagName from material-tags endpoint"
            );
        }

        // 🔍 toolUnits
        JsonNode firstToolUnit = materialsFinancingsToolsClient.getFirstToolUnit(root);
        Assertions.assertNotNull(firstToolUnit, "Expected at least one toolUnit in response");

        System.out.println("FIRST TOOL UNIT NODE: " + firstToolUnit.toPrettyString());

        // зберігаємо id toolUnit для апдейта
        JsonNode toolUnitIdNode = firstToolUnit.get("id");
        Assertions.assertNotNull(toolUnitIdNode, "toolUnit.id must not be null");
        toolUnitId = toolUnitIdNode.asText();
        Assertions.assertFalse(toolUnitId.isEmpty(), "toolUnit.id must not be empty");

        // 🔹 LOCATION: location.id має дорівнювати LOCATION_ID_WAREHOUSE_MAIN
        JsonNode locationNode = firstToolUnit.get("location");
        Assertions.assertNotNull(
                locationNode,
                "location object not found in firstToolUnit. Actual node: " + firstToolUnit.toPrettyString()
        );

        JsonNode locationIdNode = locationNode.get("id");
        Assertions.assertNotNull(
                locationIdNode,
                "location.id not found in location object. Actual location node: " + locationNode.toPrettyString()
        );

        Assertions.assertEquals(
                LOCATION_ID_WAREHOUSE_MAIN,
                locationIdNode.asText(),
                "location.id must match warehouse location id from request"
        );

        // 🔹 SUPPLIER: supplier.id має дорівнювати supplierId з /supplier-contacts
        JsonNode supplierNode = firstToolUnit.get("supplier");
        Assertions.assertNotNull(
                supplierNode,
                "supplier object not found in firstToolUnit"
        );

        JsonNode supplierIdNode = supplierNode.get("id");
        Assertions.assertNotNull(
                supplierIdNode,
                "supplier.id not found in supplier object"
        );

        Assertions.assertEquals(
                supplierId,
                supplierIdNode.asText(),
                "supplier.id must match supplierId from /supplier-contacts"
        );

        // 🔹 решта полів, які точно є
        Assertions.assertEquals(
                "AVAILABLE",
                firstToolUnit.get("status").asText(),
                "toolUnit.status must be AVAILABLE"
        );

        // purchaseDate – не пустий
        Assertions.assertTrue(
                firstToolUnit.hasNonNull("purchaseDate")
                        && !firstToolUnit.get("purchaseDate").asText().isEmpty(),
                "purchaseDate should not be null or empty"
        );
    }

    // 2️⃣ UPDATE: PUT /materials-financings/tools/{id}
    @Test
    @Order(2)
    void updateToolsFinancing_updatesMainFieldsButKeepsLinks() throws IOException {
        Assertions.assertNotNull(financingId, "financingId is null – create test probably failed");
        Assertions.assertNotNull(toolUnitId, "toolUnitId is null – create test probably failed");

        Map<String, Object> body = buildUpdateToolsFinancingBody();

        APIResponse response = materialsFinancingsToolsClient.updateToolsFinancing(financingId, body);
        int status = response.status();

        System.out.println("UPDATE /materials-financings/tools status: " + status);
        System.out.println("UPDATE /materials-financings/tools body: " + response.text());

        Assertions.assertEquals(
                200,
                status,
                "Expected 200 OK from PUT /materials-financings/tools/{id}, but got: " + status
        );

        JsonNode root = materialsFinancingsToolsClient.parseFinancing(response);

        // id фінансування не змінюється
        Assertions.assertEquals(
                financingId,
                root.get("id").asText(),
                "Updated financing id must remain the same as created"
        );

        // базові поля оновлені
        Assertions.assertEquals(
                body.get("name"),
                root.get("name").asText(),
                "Updated name must match request"
        );
        Assertions.assertEquals(
                body.get("description"),
                root.get("description").asText(),
                "Updated description must match request"
        );
        Assertions.assertEquals(
                body.get("mfg"),
                root.get("mfg").asText(),
                "Updated mfg must match request"
        );

        // 🔍 tags — усе ще той самий тег
        JsonNode tags = root.get("tags");
        Assertions.assertNotNull(tags, "tags should not be null after update");
        Assertions.assertTrue(tags.isArray(), "tags should be an array after update");
        Assertions.assertFalse(tags.isEmpty(), "Expected at least one tag after update");
        JsonNode firstTagResp = tags.get(0);
        Assertions.assertEquals(
                materialTagId,
                firstTagResp.get("id").asText(),
                "tag.id after update must still match materialTagId from material-tags endpoint"
        );

        // 🔍 toolUnits — перевіряємо, що лінки збереглися, а поля оновилися
        JsonNode firstToolUnit = materialsFinancingsToolsClient.getFirstToolUnit(root);
        Assertions.assertNotNull(firstToolUnit, "Expected at least one toolUnit in response after update");

        System.out.println("UPDATED FIRST TOOL UNIT NODE: " + firstToolUnit.toPrettyString());

        // той самий toolUnitId
        Assertions.assertEquals(
                toolUnitId,
                firstToolUnit.get("id").asText(),
                "toolUnit.id after update must remain the same"
        );

        // LOCATION
        JsonNode locationNode = firstToolUnit.get("location");
        Assertions.assertNotNull(locationNode, "location object not found in updated firstToolUnit");
        JsonNode locationIdNode = locationNode.get("id");
        Assertions.assertNotNull(locationIdNode, "location.id not found in updated location object");
        Assertions.assertEquals(
                LOCATION_ID_WAREHOUSE_MAIN,
                locationIdNode.asText(),
                "location.id after update must still match warehouse location id"
        );

        // SUPPLIER
        JsonNode supplierNode = firstToolUnit.get("supplier");
        Assertions.assertNotNull(supplierNode, "supplier object not found in updated firstToolUnit");
        JsonNode supplierIdNode = supplierNode.get("id");
        Assertions.assertNotNull(supplierIdNode, "supplier.id not found in updated supplier object");
        Assertions.assertEquals(
                supplierId,
                supplierIdNode.asText(),
                "supplier.id after update must still match supplierId from /supplier-contacts"
        );

    }

    // 3️⃣ DELETE: DELETE /materials-financings/tools/{id}
    @Test
    @Order(3)
    void deleteToolsFinancing_deletesById() {
        Assertions.assertNotNull(financingId, "financingId is null – previous tests probably failed");

        APIResponse response = materialsFinancingsToolsClient.deleteToolsFinancing(financingId);
        int status = response.status();

        System.out.println("DELETE /materials-financings/tools/{id} status: " + status);
        System.out.println("DELETE /materials-financings/tools/{id} body: '" + response.text() + "'");

        Assertions.assertEquals(
                204,
                status,
                "Expected 204 No Content from DELETE /materials-financings/tools/{id}, but got: " + status
        );
    }

    // ---------- helpers ----------

    private Map<String, Object> buildCreateToolsFinancingBody() {
        Map<String, Object> body = new HashMap<>();
        long ts = System.currentTimeMillis();

        body.put("name", "API Materials Financing Tool " + ts);
        body.put("description", "Created via API E2E materials-financings/tools test");
        body.put("mfg", "API-MFG-" + ts);

        // 🔹 реальний material tag з бекенду
        Map<String, Object> tag = new HashMap<>();
        tag.put("id", materialTagId);
        if (materialTagName != null) {
            tag.put("name", materialTagName);
        }
        body.put("tags", List.of(tag));

        // toolUnit
        Map<String, Object> toolUnit = new HashMap<>();
        toolUnit.put("name", "API Tool Unit " + ts);
        toolUnit.put("note", "Created from materials-financings/tools E2E test");

        toolUnit.put("barcodes", List.of("API-BC-" + ts));
        toolUnit.put("jobIds", Collections.emptyList());

        toolUnit.put("locationId", LOCATION_ID_WAREHOUSE_MAIN);

        Map<String, Object> locationPosition = new HashMap<>();
        locationPosition.put("aisle", "A1");
        locationPosition.put("bay", "B1");
        locationPosition.put("bin", "BIN1");
        locationPosition.put("level", "L1");
        toolUnit.put("locationPosition", locationPosition);

        toolUnit.put("purchaseCost", 100.0);
        toolUnit.put("purchaseValue", 100.0);

        // формат як у createdAt/updatedAt: без таймзони
        String purchaseDate = LocalDateTime.now()
                .truncatedTo(ChronoUnit.MICROS)
                .toString();
        toolUnit.put("purchaseDate", purchaseDate);

        toolUnit.put("serialNumber", "SN-" + ts);
        toolUnit.put("status", "AVAILABLE");
        toolUnit.put("supplierId", supplierId);
        // userId бек, судячи з усього, не вимагає

        body.put("toolUnits", List.of(toolUnit));

        return body;
    }

    private Map<String, Object> buildUpdateToolsFinancingBody() {
        Map<String, Object> body = new HashMap<>();
        long ts = System.currentTimeMillis();

        body.put("id", financingId); // можна й не слати, але за схемою він є
        body.put("name", "API Materials Financing Tool UPDATED " + ts);
        body.put("description", "Updated via API E2E materials-financings/tools test");
        body.put("mfg", "API-MFG-UPDATED-" + ts);

        Map<String, Object> tag = new HashMap<>();
        tag.put("id", materialTagId);
        if (materialTagName != null) {
            tag.put("name", materialTagName);
        }
        body.put("tags", List.of(tag));

        Map<String, Object> toolUnit = new HashMap<>();
        toolUnit.put("id", toolUnitId); // 🔹 обов'язково, щоб апдейтнув існуючий
        toolUnit.put("name", "API Tool Unit UPDATED " + ts);
        toolUnit.put("note", "Updated from materials-financings/tools E2E test");

        toolUnit.put("barcodes", List.of("API-BC-UPDATED-" + ts));
        toolUnit.put("jobIds", Collections.emptyList());

        toolUnit.put("locationId", LOCATION_ID_WAREHOUSE_MAIN);

        Map<String, Object> locationPosition = new HashMap<>();
        locationPosition.put("aisle", "A2");
        locationPosition.put("bay", "B2");
        locationPosition.put("bin", "BIN2");
        locationPosition.put("level", "L2");
        toolUnit.put("locationPosition", locationPosition);

        toolUnit.put("purchaseCost", 200.0);
        toolUnit.put("purchaseValue", 200.0);

        String purchaseDate = LocalDateTime.now()
                .truncatedTo(ChronoUnit.MICROS)
                .toString();
        toolUnit.put("purchaseDate", purchaseDate);

        toolUnit.put("serialNumber", "SN-UPDATED-" + ts);
        toolUnit.put("status", "AVAILABLE");
        toolUnit.put("supplierId", supplierId);

        body.put("toolUnits", List.of(toolUnit));

        return body;
    }
}
