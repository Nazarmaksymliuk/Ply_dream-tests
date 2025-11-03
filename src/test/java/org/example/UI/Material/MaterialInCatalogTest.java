package org.example.UI.Material;

import org.example.BaseUITestExtension.PlaywrightUiLoginBaseTest;
import org.example.UI.PageObjectModels.Alerts.AlertUtils;
import org.example.UI.PageObjectModels.Catalog.CatalogPage;
import org.example.UI.PageObjectModels.Material.MaterialsCreationFlow.MaterialSpecsPage;
import org.example.UI.PageObjectModels.Material.MaterialsCreationFlow.PriceAndVariantsPage;
import org.example.UI.PageObjectModels.Material.MaterialsCreationFlow.MaterialStockSetupPage;
import org.example.UI.PageObjectModels.Material.MaterialPage;
import org.example.UI.PageObjectModels.Material.MaterialsListPage;
import org.junit.jupiter.api.*;

import org.assertj.core.api.Assertions;
import org.example.UI.Models.Material;

import java.util.Random;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MaterialInCatalogTest extends PlaywrightUiLoginBaseTest {
    MaterialPage materialPage;
    CatalogPage catalogPage;
    MaterialSpecsPage materialSpecsPage;
    PriceAndVariantsPage priceAndVariantsPage;
    MaterialStockSetupPage stockSetupPage;
    MaterialsListPage materialsListPage;

    private final String defaultVariation = "Single";
    private final String defaultUnitOfMeasurement = "Ea";
    private final String warehouse = "WarehouseMain";

    @BeforeEach
    public void setUp() {
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

    @DisplayName("Create material in Catalog Test")
    @Order(0)
    @Test
    public void createMaterialInCatalogTest(){

        catalogPage.waitForLoaded();
        materialSpecsPage =
                catalogPage.clickAddItem(MaterialSpecsPage.class);

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

        stockSetupPage.selectWarehouse(warehouse);
        stockSetupPage.setQuantity(material.quantity);
        stockSetupPage.clickSaveLocationButton();

        stockSetupPage.clickSaveButton();

        AlertUtils.waitForAlertVisible(page);
        String alert = AlertUtils.getAlertText(page);
        Assertions.assertThat(alert).isEqualTo("%s has been successfully added", material.name);
        AlertUtils.waitForAlertHidden(page);

        Assertions.assertThat(materialsListPage.getFirstMaterialNameInTheList()).isEqualTo(material.name);
        Assertions.assertThat(materialsListPage.getFirstItemNumberInTheList()).isEqualTo(material.itemNumber);

        materialsListPage.clickFirstLocationArrowDown();

        Assertions.assertThat(materialsListPage.getMaterialLocationFromDropdown()).contains(warehouse);
        Assertions.assertThat(materialsListPage.getFirstMaterialVariation()).isEqualTo(material.variationName);
        Assertions.assertThat(materialsListPage.getQtyFromMaterialLocation()).isEqualTo(material.quantity);
    }

    Material editedMaterial = new Material(
            "Material-edited" + new Random().nextInt(100000),
            "ITEM-edited" + new Random().nextInt(100000),
            "Sample description-edited",
            "BrandXXX-edited",
            "ManufacturerYYY-edited",
            "CategoryZZZ",
            defaultUnitOfMeasurement,
            defaultVariation,
            "Single Description-edited",
            25.5,
            15.5,
            1000
    );


    @DisplayName("Update Material in the Catalog")
    @Order(1)
    @Test
    public void updateMaterialInCatalogTest(){
        catalogPage.waitForLoaded();

        String firstNameForEditing = materialsListPage.getFirstMaterialNameInTheList();

        catalogPage.openFirstRowMaterialThreeDots();
        materialSpecsPage = catalogPage.chooseMenuEditMaterial();

        waitForElementPresent(firstNameForEditing);

        // 3️⃣ Заповнити основну інформацію
        materialSpecsPage.setMaterialName(editedMaterial.name);
        materialSpecsPage.setItemNumber(editedMaterial.itemNumber);
        materialSpecsPage.setDescription(editedMaterial.description);
        materialSpecsPage.setBrand(editedMaterial.brand);
        materialSpecsPage.setManufacturer(editedMaterial.manufacturer);

        materialSpecsPage.clickSaveButtonInTheEditMaterialFlow();

        waitForElementPresent(editedMaterial.name);
        Assertions.assertThat(materialsListPage.getFirstMaterialNameInTheList()).isEqualTo(editedMaterial.name);
        Assertions.assertThat(materialsListPage.getFirstItemNumberInTheList()).isEqualTo(editedMaterial.itemNumber);

    }

    @DisplayName("Delete Material Test")
    @Order(2)
    @Test
    public void deleteMaterialInCatalogTest(){
        catalogPage.waitForLoaded();

        String firstNameForDeleting = materialsListPage.getFirstMaterialNameInTheList();

        catalogPage.openFirstRowMaterialThreeDots();
        catalogPage.chooseMenuDeleteMaterial();
        catalogPage.confirmDeleteMaterialInModal();

        AlertUtils.waitForAlertVisible(page);
        String alert = AlertUtils.getAlertText(page);
        Assertions.assertThat(alert).isEqualTo("Material deleted successfully");
        AlertUtils.waitForAlertHidden(page);

        waitForElementRemoved(firstNameForDeleting);
        Assertions.assertThat(materialsListPage.getMaterialNamesList()).doesNotContain(firstNameForDeleting);
    }




}
