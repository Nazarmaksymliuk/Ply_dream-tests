package org.example.Api.Materials;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.APIResponse;
import org.example.Api.helpers.MaterialsHelper.MaterialsClient;
import org.example.BaseAPITestExtension.BaseApiTest;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MaterialE2ETests extends BaseApiTest {

    private MaterialsClient materialsClient;
    private String materialId; // сюди збережемо id створеного матеріалу

    @BeforeAll
    void initClient() {
        materialsClient = new MaterialsClient(userApi);
    }

    @Test
    @Order(1)
    void createMaterial_createsAndStoresId() throws IOException {
        Map<String, Object> body = buildCreateMaterialRequest();

        APIResponse response = materialsClient.createMaterial(body);
        int status = response.status();

        System.out.println("CREATE status: " + status);
        System.out.println("CREATE body: " + response.text());

        Assertions.assertTrue(
                status == 200 || status == 201,
                "Expected 200 or 201 on create, but got: " + status
        );

        materialId = materialsClient.extractMaterialId(response);
        Assertions.assertNotNull(materialId, "materialId must not be null after create");
        Assertions.assertFalse(materialId.isEmpty(), "materialId must not be empty");
    }

    @Test
    @Order(2)
    void updateMaterial_updatesPreviouslyCreated() throws IOException {
        Assertions.assertNotNull(materialId, "materialId is null – create test probably failed");

        Map<String, Object> updateBody = buildUpdateMaterialRequest(materialId);

        APIResponse response = materialsClient.updateMaterial(materialId, updateBody);
        int status = response.status();

        System.out.println("UPDATE status: " + status);
        System.out.println("UPDATE body: " + response.text());

        Assertions.assertEquals(200, status, "Expected 200 on update");

        JsonNode materialNode = materialsClient.extractMaterialNode(response);
        Assertions.assertNotNull(materialNode, "Updated response should contain 'material'");

        String actualName = materialNode.get("name").asText();
        String expectedName = (String) updateBody.get("name");
        Assertions.assertEquals(expectedName, actualName, "Updated name must match request");
    }

    @Test
    @Order(3)
    void deleteMaterial_deletesPreviouslyCreated() {
        Assertions.assertNotNull(materialId, "materialId is null – create test probably failed");

        APIResponse response = materialsClient.deleteMaterial(materialId);
        int status = response.status();

        System.out.println("DELETE status: " + status);
        System.out.println("DELETE body: " + response.text());

        Assertions.assertTrue(
                status == 200 || status == 204,
                "Expected 200 or 204 on delete, but got: " + status
        );
    }

    // ---------- helpers ----------

    private static Map<String, Object> buildCreateMaterialRequest() {
        Map<String, Object> body = new HashMap<>();

        body.put("active", true);
        body.put("name", "API CRUD Material " + System.currentTimeMillis());
        body.put("description", "Created via CRUD API test");
        body.put("itemNumber", "CRUD-" + System.currentTimeMillis());
        body.put("brand", "API-CRUD-Brand");
        body.put("manufacturer", "API-CRUD-Manufacturer");

        //body.put("materialType", "TEMPORARY_LINE_ITEM");
        body.put("leadTime", "EIGHTEEN_WEEKS");
        body.put("serialized", false);

        Map<String, Object> measurementUnit = new HashMap<>();
        measurementUnit.put("name", "Each");
        measurementUnit.put("abbreviation", "EA");
        measurementUnit.put("creationSource", "PLY");
        body.put("measurementUnit", measurementUnit);

        return body;
    }

    private static Map<String, Object> buildUpdateMaterialRequest(String materialId) {
        Map<String, Object> body = new HashMap<>();

        body.put("id", materialId);
        body.put("active", true);
        body.put("name", "API CRUD Material UPDATED " + System.currentTimeMillis());
        body.put("description", "Updated via CRUD API test");
        body.put("itemNumber", "CRUD-UPD-" + System.currentTimeMillis());
        body.put("brand", "API-CRUD-Brand-Updated");
        body.put("manufacturer", "API-CRUD-Manufacturer-Updated");

        //body.put("materialType", "TEMPORARY_LINE_ITEM");
        body.put("leadTime", "EIGHTEEN_WEEKS");
        body.put("serialized", false);

        Map<String, Object> measurementUnit = new HashMap<>();
        measurementUnit.put("name", "Each");
        measurementUnit.put("abbreviation", "EA");
        measurementUnit.put("creationSource", "PLY");
        body.put("measurementUnit", measurementUnit);

        return body;
    }
}
