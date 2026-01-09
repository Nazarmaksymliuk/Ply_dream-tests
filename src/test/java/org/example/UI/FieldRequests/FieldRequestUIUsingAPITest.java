package org.example.UI.FieldRequests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIResponse;
import org.example.Api.helpers.LocationsHelper.LocationsClient;
import org.example.Api.helpers.MaterialsHelper.MaterialsClient;
import org.example.BaseUIApiExtension.PlaywrightUiApiBaseTest;
import org.example.UI.PageObjectModels.FieldRequests.FieldRequestsPage;
import org.example.apifactories.LocationsTestDataFactory;
import org.example.apifactories.MaterialsTestDataFactory;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.Random;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FieldRequestUIUsingAPITest extends PlaywrightUiApiBaseTest {

    private FieldRequestsPage fr;

    private LocationsClient locationsClient;
    private MaterialsClient materialsClient;

    private String warehouseId;
    private String warehouseName;

    private String materialId;
    private String materialVariationId;

    private final ObjectMapper om = new ObjectMapper();

    private final String fieldRequestName = "AQA-FR-" + new Random().nextInt(100000);
    private final String editedFieldRequestName = "Edited-AQA-FR-" + new Random().nextInt(100000);

    @BeforeAll
    void beforeAll_createWarehouseAndMaterialAttached() throws IOException {
        locationsClient = new LocationsClient(userApi);
        materialsClient = new MaterialsClient(userApi);

        // 1) CREATE WAREHOUSE
        Map<String, Object> warehouseBody =
                LocationsTestDataFactory.buildCreateWarehouseBody("UI-FR Warehouse ");

        APIResponse whResponse = locationsClient.createLocation(warehouseBody, false);

        System.out.println("BEFOREALL CREATE WAREHOUSE status: " + whResponse.status());
        System.out.println("BEFOREALL CREATE WAREHOUSE body: " + whResponse.text());

        Assertions.assertTrue(whResponse.status() == 201 || whResponse.status() == 200, "Warehouse should be created");

        JsonNode createdWarehouse = locationsClient.parseLocation(whResponse);
        warehouseId = createdWarehouse.get("id").asText();
        warehouseName = createdWarehouse.get("name").asText();

        Assertions.assertNotNull(warehouseId);
        Assertions.assertFalse(warehouseId.isBlank());
        Assertions.assertNotNull(warehouseName);
        Assertions.assertFalse(warehouseName.isBlank());

        // 2) CREATE MATERIAL (CATALOG)
        Map<String, Object> createMaterialBody = MaterialsTestDataFactory.buildCreateMaterialInLocationRequest(
                "UI-FR Material ",
                "UI-FR-",
                warehouseId // фактично тут не критично, але не чіпаємо фабрику
        );

        System.out.println("BEFOREALL CREATE MATERIAL request body: " + createMaterialBody);

        APIResponse createMatResp = materialsClient.createMaterial(createMaterialBody);

        System.out.println("BEFOREALL CREATE MATERIAL status: " + createMatResp.status());
        System.out.println("BEFOREALL CREATE MATERIAL body: " + createMatResp.text());

        Assertions.assertTrue(createMatResp.status() == 201 || createMatResp.status() == 200, "Material should be created");

        materialId = materialsClient.extractMaterialId(createMatResp);
        materialVariationId = materialsClient.extractFirstVariationId(createMatResp);

        Assertions.assertNotNull(materialId);
        Assertions.assertFalse(materialId.isBlank());
        Assertions.assertNotNull(materialVariationId);
        Assertions.assertFalse(materialVariationId.isBlank());

        // 3) ATTACH MATERIAL TO WAREHOUSE (LOCATION)
        Map<String, Object> attachBody = MaterialsTestDataFactory.buildAttachMaterialToLocationRequest(
                warehouseId,
                materialId,
                materialVariationId,
                1
        );

        System.out.println("BEFOREALL ATTACH MATERIAL request body: " + attachBody);

        APIResponse attachResp = materialsClient.attachMaterialToLocation(attachBody);

        System.out.println("BEFOREALL ATTACH MATERIAL status: " + attachResp.status());
        System.out.println("BEFOREALL ATTACH MATERIAL body: " + attachResp.text());

        Assertions.assertTrue(
                attachResp.status() == 200 || attachResp.status() == 201 || attachResp.status() == 204,
                "Material should be attached to warehouse"
        );

        // 4) (Optional but useful) VERIFY via GET that material is attached
        APIResponse getResp = materialsClient.getMaterial(materialId);
        System.out.println("BEFOREALL GET MATERIAL status: " + getResp.status());
        Assertions.assertEquals(200, getResp.status());

        JsonNode root = om.readTree(getResp.text());
        JsonNode mdl = root.path("materialVariations").path(0).path("materialDetailsWithLocations");

        Assertions.assertTrue(mdl.isArray() && mdl.size() > 0,
                "Material created but NOT attached to location (materialDetailsWithLocations empty)");

        String attachedLocationId = mdl.get(0).path("location").path("id").asText();
        Assertions.assertEquals(warehouseId, attachedLocationId, "Material attached to wrong location");
    }

    @AfterAll
    void afterAll_cleanupWarehouseAndMaterial() {
        if (materialId != null && !materialId.isBlank()) {
            try {
                APIResponse delMat = materialsClient.deleteMaterial(materialId);
                System.out.println("AFTERALL DELETE MATERIAL status: " + delMat.status());
                System.out.println("AFTERALL DELETE MATERIAL body: " + delMat.text());
                Assertions.assertTrue(delMat.status() == 204 || delMat.status() == 200);
            } catch (Exception e) {
                System.out.println("AFTERALL DELETE MATERIAL failed: " + e.getMessage());
            }
        }

        if (warehouseId != null && !warehouseId.isBlank()) {
            try {
                APIResponse delLoc = locationsClient.deleteLocation(
                        warehouseId,
                        null,
                        "Delete warehouse via UI-FR test cleanup"
                );
                System.out.println("AFTERALL DELETE WAREHOUSE status: " + delLoc.status());
                System.out.println("AFTERALL DELETE WAREHOUSE body: " + delLoc.text());
                Assertions.assertTrue(delLoc.status() == 204 || delLoc.status() == 200);
            } catch (Exception e) {
                System.out.println("AFTERALL DELETE WAREHOUSE failed: " + e.getMessage());
            }
        }
    }

    @BeforeEach
    void setUp() {
        openPath("/field-requests");
        fr = new FieldRequestsPage(page);
    }

    @Test
    @Order(0)
    @DisplayName("Creating FR (warehouse + material attached via API)")
    void createFieldRequest_warehouse_success() {

        fr.startCreate()
                .selectLocation(warehouseName)
                .setName(fieldRequestName)
                .chooseDate(LocalDate.now())
                .setNotes("test note")
                .continueNext()
                .addFirstMaterialFromSearch() // material attached => should appear
                .setQuantityAndSave("10");

        fr.assertFieldRequestListed(fieldRequestName);
    }

    @Test
    @Order(1)
    @DisplayName("Updating FR")
    void updateFieldRequest_warehouse_success() {
        fr.openEditMenuOption()
                .setEditedName(editedFieldRequestName)
                .setEditedNotes("test note edited " + editedFieldRequestName)
                .saveChanges();

        fr.assertFieldRequestListed(editedFieldRequestName);
    }

    @Test
    @Order(2)
    @DisplayName("Deleting FR")
    void deleteFieldRequest_warehouse_success() {
        fr.openDeleteAndConfirmMenuOption();
        fr.assertFieldRequestNotListed(editedFieldRequestName);
    }
}
