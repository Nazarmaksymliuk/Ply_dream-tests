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
import org.example.fixtures.WarehouseMaterialApiFixture;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.Random;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FieldRequestUIUsingAPITest extends PlaywrightUiApiBaseTest {
    private WarehouseMaterialApiFixture fixture;
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
    void beforeAll() throws IOException {
        fixture = WarehouseMaterialApiFixture.create(userApi)
                .provisionWarehouseWithAttachedMaterial();

        warehouseId = fixture.warehouseId();
        warehouseName = fixture.warehouseName();
        materialId = fixture.materialId();
        materialVariationId = fixture.materialVariationId();
    }

    @AfterAll
    void afterAll() {
        if (fixture != null) fixture.cleanup();
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
