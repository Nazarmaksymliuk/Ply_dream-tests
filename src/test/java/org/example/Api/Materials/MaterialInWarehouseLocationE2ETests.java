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
    private static String materialVariationId;

    @BeforeAll
    void beforeAll() throws IOException {
        locationsClient = new LocationsClient(userApi);
        materialsClient = new MaterialsClient(userApi);

        Map<String, Object> body =
                LocationsTestDataFactory.buildCreateWarehouseBody("API Warehouse for Material E2E ");

        APIResponse response = locationsClient.createLocation(body, false);

        System.out.println("BEFOREALL CREATE WAREHOUSE status: " + response.status());
        System.out.println("BEFOREALL CREATE WAREHOUSE body: " + response.text());

        Assertions.assertTrue(response.status() == 201 || response.status() == 200);

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
    void createMaterialAndAttachToWarehouseLocation() throws IOException {
        // 1) Create material (catalog)
        Map<String, Object> createBody = MaterialsTestDataFactory.buildCreateMaterialInLocationRequest(
                "API LOC Material ",
                "LOC-",
                locationId // тут можеш навіть не передавати, але лишаємо як було
        );

        System.out.println("CREATE MATERIAL REQUEST body: " + createBody);

        APIResponse createResp = materialsClient.createMaterial(createBody);

        System.out.println("CREATE MATERIAL status: " + createResp.status());
        System.out.println("CREATE MATERIAL body: " + createResp.text());

        Assertions.assertTrue(createResp.status() == 200 || createResp.status() == 201);

        materialId = materialsClient.extractMaterialId(createResp);
        materialVariationId = materialsClient.extractFirstVariationId(createResp);

        Assertions.assertNotNull(materialId);
        Assertions.assertFalse(materialId.isEmpty());
        Assertions.assertNotNull(materialVariationId);
        Assertions.assertFalse(materialVariationId.isEmpty());

        // 2) Attach material to location via /locations/materials
        Map<String, Object> attachBody = MaterialsTestDataFactory.buildAttachMaterialToLocationRequest(
                locationId,
                materialId,
                materialVariationId,
                1
        );

        System.out.println("ATTACH MATERIAL REQUEST body: " + attachBody);

        APIResponse attachResp = materialsClient.attachMaterialToLocation(attachBody);

        System.out.println("ATTACH MATERIAL status: " + attachResp.status());
        System.out.println("ATTACH MATERIAL body: " + attachResp.text());

        Assertions.assertTrue(
                attachResp.status() == 200 || attachResp.status() == 201 || attachResp.status() == 204,
                "Expected attach to succeed"
        );

        // 3) Verify attachment by GET material
        APIResponse getResp = materialsClient.getMaterial(materialId);

        System.out.println("GET MATERIAL status: " + getResp.status());
        System.out.println("GET MATERIAL body: " + getResp.text());

        Assertions.assertEquals(200, getResp.status());

        JsonNode root = new com.fasterxml.jackson.databind.ObjectMapper().readTree(getResp.text());
        JsonNode mdl = root.path("materialVariations").path(0).path("materialDetailsWithLocations");

        Assertions.assertTrue(mdl.isArray() && mdl.size() > 0,
                "Material was created but NOT attached to location (materialDetailsWithLocations is empty)");

        String attachedLocationId = mdl.get(0).path("location").path("id").asText();
        Assertions.assertEquals(locationId, attachedLocationId, "Attached to wrong location");
    }

    @Test
    @Order(2)
    void updateMaterial_updatesPreviouslyCreated() throws IOException {
        Assertions.assertNotNull(materialId, "materialId is null – create test probably failed");

        Map<String, Object> updateBody = MaterialsTestDataFactory.buildUpdateMaterialRequest(
                materialId,
                "API LOC Material UPDATED ",
                "LOC-UPD-"
        );

        updateBody.put("name", updateBody.get("name"));
        updateBody.put("itemNumber", updateBody.get("itemNumber"));

        System.out.println("UPDATE REQUEST body: " + updateBody);

        APIResponse response = materialsClient.updateMaterial(materialId, updateBody);

        System.out.println("UPDATE MATERIAL status: " + response.status());
        System.out.println("UPDATE MATERIAL body: " + response.text());

        Assertions.assertEquals(200, response.status(), "Expected 200 on update");

        JsonNode materialNode = materialsClient.extractMaterialNode(response);
        Assertions.assertNotNull(materialNode);

        Assertions.assertEquals(updateBody.get("name"), materialNode.get("name").asText());
    }

    @Test
    @Order(3)
    void deleteMaterial_deletesPreviouslyCreated() {
        Assertions.assertNotNull(materialId, "materialId is null – create test probably failed");

        APIResponse response = materialsClient.deleteMaterial(materialId);

        System.out.println("DELETE MATERIAL status: " + response.status());
        System.out.println("DELETE MATERIAL body: " + response.text());

        Assertions.assertTrue(response.status() == 200 || response.status() == 204);

        materialId = null;
        materialVariationId = null;
    }
}
