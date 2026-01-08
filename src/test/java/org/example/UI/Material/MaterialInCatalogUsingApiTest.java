package org.example.UI.Material;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.APIResponse;
import org.assertj.core.api.Assertions;
import org.example.Api.helpers.LocationsHelper.LocationsClient;
import org.example.BaseUIApiExtension.PlaywrightUiApiBaseTest;
import org.example.UI.Models.Material;
import org.example.UI.PageObjectModels.Alerts.AlertUtils;
import org.example.UI.PageObjectModels.Catalog.CatalogPage;
import org.example.UI.PageObjectModels.Material.MaterialPage;
import org.example.UI.PageObjectModels.Material.MaterialsCreationFlow.MaterialSpecsPage;
import org.example.UI.PageObjectModels.Material.MaterialsCreationFlow.MaterialStockSetupPage;
import org.example.UI.PageObjectModels.Material.MaterialsCreationFlow.PriceAndVariantsPage;
import org.example.UI.PageObjectModels.Material.MaterialsListPage;
import org.example.apifactories.LocationsTestDataFactory;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.Random;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MaterialInCatalogUsingApiTest extends PlaywrightUiApiBaseTest {

    private static LocationsClient locationsClient;
    private static String warehouseId;
    private static String warehouseName;

    MaterialPage materialPage;
    CatalogPage catalogPage;
    MaterialSpecsPage materialSpecsPage;
    PriceAndVariantsPage priceAndVariantsPage;
    MaterialStockSetupPage stockSetupPage;
    MaterialsListPage materialsListPage;

    private final String defaultVariation = "Single";
    private final String defaultUnitOfMeasurement = "Ea";

    @BeforeAll
    void createWarehouse() throws IOException {
        locationsClient = new LocationsClient(userApi);

        APIResponse response = locationsClient.createLocation(
                LocationsTestDataFactory.buildCreateWarehouseBody("UI-CATALOG-E2E "),
                false
        );

        JsonNode created = locationsClient.parseLocation(response);
        warehouseId = created.get("id").asText();
        warehouseName = created.get("name").asText();

        org.junit.jupiter.api.Assertions.assertNotNull(warehouseId);
        org.junit.jupiter.api.Assertions.assertFalse(warehouseId.isBlank());
    }

    @AfterAll
    void deleteWarehouse() {
        if (warehouseId == null) return;

        locationsClient.deleteLocation(
                warehouseId,
                null,
                "Cleanup after MaterialInCatalogTest"
        );
    }

    @BeforeEach
    void setUp() {
        openPath("/catalog");
        catalogPage = new CatalogPage(page);
        materialPage = new MaterialPage(page);
        materialsListPage = new MaterialsListPage(page);
    }

    Material material = new Material(
            "Material" + new Random().nextInt(100000),
            "ITEM-" + new Random().nextInt(100000),
            "Sample description",
            "BrandXXX",
            "ManufacturerYYY",
            "CategoryZZZ",
            defaultUnitOfMeasurement,
            defaultVariation,
            "Single Description",
            25.5,
            15.5,
            10
    );
    Material materialUpdated = new Material(
            "Updated-Material" + new Random().nextInt(100000),
            "ITEM-" + new Random().nextInt(100000),
            "Sample description",
            "BrandXXX",
            "ManufacturerYYY",
            "CategoryZZZ",
            defaultUnitOfMeasurement,
            defaultVariation,
            "Single Description",
            25.5,
            15.5,
            10
    );

    @Test
    @Order(0)
    void createMaterial() {
        catalogPage.waitForLoaded();
        materialSpecsPage = catalogPage.clickAddItem(MaterialSpecsPage.class);

        materialSpecsPage.setMaterialName(material.name);
        materialSpecsPage.setItemNumber(material.itemNumber);
        materialSpecsPage.setDescription(material.description);
        materialSpecsPage.setBrand(material.brand);
        materialSpecsPage.setManufacturer(material.manufacturer);

        materialSpecsPage.clickAddMaterialVariantButton();
        materialSpecsPage.setVariantName(material.variationName);
        materialSpecsPage.setVariantDescription(material.variationDescription);

        priceAndVariantsPage = materialSpecsPage.clickNextButton();
        priceAndVariantsPage.setCostForClient(material.costForClient);
        priceAndVariantsPage.setCostForBusiness(material.costForBusiness);

        stockSetupPage = priceAndVariantsPage.clickNextButton();
        stockSetupPage.clickAddLocationButton();
        stockSetupPage.clickChooseLocationButton();

        // ✅ Динамічний склад з API
        stockSetupPage.selectWarehouse(warehouseName);

        stockSetupPage.setQuantity(material.quantity);
        stockSetupPage.clickSaveLocationButton();
        stockSetupPage.clickSaveButton();

        AlertUtils.waitForAlertVisible(page);
        Assertions.assertThat(AlertUtils.getAlertText(page)).contains("successfully");
        AlertUtils.waitForAlertHidden(page);
    }

    @Test
    @Order(1)
    void updateMaterial() {
        catalogPage.waitForLoaded();

        String name = materialsListPage.getFirstMaterialNameInTheList();
        catalogPage.openFirstRowMaterialThreeDots();
        materialSpecsPage = catalogPage.chooseMenuEditMaterial();

        waitForElementPresent(name);

        materialSpecsPage.setMaterialName(materialUpdated.name);
        materialSpecsPage.clickSaveButtonInTheEditMaterialFlow();

        waitForElementPresent(materialUpdated.name);

        Assertions.assertThat(materialsListPage.getFirstMaterialNameInTheList())
                .isEqualTo(materialUpdated.name);
    }

    @Test
    @Order(2)
    void deleteMaterial() {
        catalogPage.waitForLoaded();

        String name = materialsListPage.getFirstMaterialNameInTheList();
        catalogPage.openFirstRowMaterialThreeDots();
        catalogPage.chooseMenuDeleteMaterial();
        catalogPage.confirmDeleteMaterialInModal();

        AlertUtils.waitForAlertVisible(page);
        Assertions.assertThat(AlertUtils.getAlertText(page)).contains("deleted");
        AlertUtils.waitForAlertHidden(page);

        waitForElementRemoved(name);
    }
}
