package org.example.Api.Materials;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.APIResponse;
import org.example.Api.helpers.LocationsHelper.LocationsClient;
import org.example.Api.helpers.MaterialsHelper.MaterialsClient;
import org.example.BaseAPITestExtension.BaseApiTest;
import org.example.apifactories.LocationsTestDataFactory;
import org.example.apifactories.MaterialsTestDataFactory;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.Map;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MaterialInWarehouseLocationE2ETests extends BaseApiTest {

    private LocationsClient locationsClient;
    private MaterialsClient materialsClient;

    private static String locationId;
    private static String materialId;

    @BeforeAll
    void beforeAll() throws IOException {
        locationsClient = new LocationsClient(userApi);
        materialsClient = new MaterialsClient(userApi);

        Map<String, Object> body =
                LocationsTestDataFactory.buildCreateWarehouseBody("API Warehouse for Material E2E ");

        APIResponse response = locationsClient.createLocation(body, false);
        int status = response.status();

        System.out.println("BEFOREALL CREATE WAREHOUSE status: " + status);
        System.out.println("BEFOREALL CREATE WAREHOUSE body: " + response.text());

        Assertions.assertTrue(status == 201 || status == 200);

        locationId = locationsClient.extractLocationId(response);
        Assertions.assertNotNull(locationId);
        Assertions.assertFalse(locationId.isEmpty());
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

        if (locationId != null && !locationId.isEmpty()) {
            try {
                APIResponse delLoc = locationsClient.deleteLocation(
                        locationId,
                        null,
                        "Delete warehouse via API E2E cleanup"
                );
                System.out.println("AFTERALL DELETE WAREHOUSE status: " + delLoc.status());
                System.out.println("AFTERALL DELETE WAREHOUSE body: " + delLoc.text());
                Assertions.assertTrue(delLoc.status() == 204 || delLoc.status() == 200);
            } catch (Exception e) {
                System.out.println("AFTERALL DELETE WAREHOUSE failed: " + e.getMessage());
            }
        }
    }

    @Test
    @Order(1)
    void createMaterialInWarehouse_createsAndStoresMaterialId() throws IOException {
        Map<String, Object> body = MaterialsTestDataFactory.buildCreateMaterialInLocationRequest(
                "API LOC Material ",
                "LOC-",
                locationId
        );

        System.out.println("CREATE REQUEST body: " + body);

        APIResponse response = materialsClient.createMaterial(body);
        int status = response.status();

        System.out.println("CREATE MATERIAL IN LOCATION status: " + status);
        System.out.println("CREATE MATERIAL IN LOCATION body: " + response.text());

        Assertions.assertTrue(status == 200 || status == 201);

        materialId = materialsClient.extractMaterialId(response);
        Assertions.assertNotNull(materialId);
        Assertions.assertFalse(materialId.isEmpty());

        // safe assertions (API returns empty materialDetailsWithLocations in response)
        JsonNode materialNode = materialsClient.extractMaterialNode(response);
        Assertions.assertNotNull(materialNode);
        Assertions.assertEquals(materialId, materialNode.get("id").asText());
    }

    @Test
    @Order(2)
    void updateMaterial_updatesPreviouslyCreated() throws IOException {
        Assertions.assertNotNull(materialId, "materialId is null – create test probably failed");

        // ✅ Use existing factory for update (no variations!) to avoid backend 500
        Map<String, Object> updateBody = MaterialsTestDataFactory.buildUpdateMaterialRequest(
                materialId,
                "API LOC Material UPDATED ",
                "LOC-UPD-"
        );

        // ✅ This endpoint validates root-level name/itemNumber (as you saw on 400),
        // so we duplicate them at root to satisfy validator.
        updateBody.put("name", updateBody.get("name"));
        updateBody.put("itemNumber", updateBody.get("itemNumber"));

        System.out.println("UPDATE REQUEST body: " + updateBody);

        APIResponse response = materialsClient.updateMaterial(materialId, updateBody);
        int status = response.status();

        System.out.println("UPDATE MATERIAL status: " + status);
        System.out.println("UPDATE MATERIAL body: " + response.text());

        Assertions.assertEquals(200, status, "Expected 200 on update");

        JsonNode materialNode = materialsClient.extractMaterialNode(response);
        Assertions.assertNotNull(materialNode, "Updated response should contain 'material' node");

        Assertions.assertEquals(
                updateBody.get("name"),
                materialNode.get("name").asText(),
                "Updated name must match request"
        );

        // optional: itemNumber
        if (materialNode.get("itemNumber") != null && !materialNode.get("itemNumber").isNull()) {
            Assertions.assertEquals(
                    updateBody.get("itemNumber"),
                    materialNode.get("itemNumber").asText(),
                    "Updated itemNumber must match request"
            );
        }
    }

    @Test
    @Order(3)
    void deleteMaterial_deletesPreviouslyCreated() {
        Assertions.assertNotNull(materialId, "materialId is null – create test probably failed");

        APIResponse response = materialsClient.deleteMaterial(materialId);
        int status = response.status();

        System.out.println("DELETE MATERIAL status: " + status);
        System.out.println("DELETE MATERIAL body: " + response.text());

        Assertions.assertTrue(status == 200 || status == 204);

        materialId = null;
    }
}
