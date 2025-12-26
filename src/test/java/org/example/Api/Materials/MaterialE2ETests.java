package org.example.Api.Materials;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.APIResponse;
import org.example.Api.helpers.MaterialsHelper.MaterialsClient;
import org.example.BaseAPITestExtension.BaseApiTest;
import org.example.apifactories.MaterialsTestDataFactory;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.Map;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MaterialE2ETests extends BaseApiTest {

    private MaterialsClient materialsClient;
    private String materialId;

    @BeforeAll
    void initClient() {
        materialsClient = new MaterialsClient(userApi);
    }

    @Test
    @Order(1)
    void createMaterial_createsAndStoresId() throws IOException {
        Map<String, Object> body = MaterialsTestDataFactory.buildCreateMaterialRequest(
                "API CRUD Material ",
                "CRUD-"
        );

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

        Map<String, Object> updateBody = MaterialsTestDataFactory.buildUpdateMaterialRequest(
                materialId,
                "API CRUD Material UPDATED ",
                "CRUD-UPD-"
        );

        APIResponse response = materialsClient.updateMaterial(materialId, updateBody);
        int status = response.status();

        System.out.println("UPDATE status: " + status);
        System.out.println("UPDATE body: " + response.text());

        Assertions.assertEquals(200, status, "Expected 200 on update");

        JsonNode materialNode = materialsClient.extractMaterialNode(response);
        Assertions.assertNotNull(materialNode, "Updated response should contain 'material'");

        Assertions.assertEquals(
                updateBody.get("name"),
                materialNode.get("name").asText(),
                "Updated name must match request"
        );
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
}
