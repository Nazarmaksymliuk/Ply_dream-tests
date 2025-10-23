package org.example.UI.Material;

import org.example.BaseTestExtension.PlaywrightBaseTest;
import org.example.PageObjectModels.Alerts.AlertUtils;
import org.example.PageObjectModels.Catalog.CatalogPage;
import org.example.PageObjectModels.Catalog.MaterialsTab.MaterialSpecsPage;
import org.example.PageObjectModels.Catalog.MaterialsTab.PriceAndVariantsPage;
import org.example.PageObjectModels.Catalog.MaterialsTab.StockSetupPage;
import org.example.PageObjectModels.Material.MaterialPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.assertj.core.api.Assertions;
import org.example.Models.Material;

import java.util.Random;

public class MaterialInCatalogTest extends PlaywrightBaseTest {
    MaterialPage materialPage;
    CatalogPage catalogPage;
    MaterialSpecsPage materialSpecsPage;
    PriceAndVariantsPage priceAndVariantsPage;
    StockSetupPage stockSetupPage;

    private final String defaultVariation = "Single";
    private final String defaultUnitOfMeasurement = "Ea";
    private final String warehouse = "WarehouseMain";

    @BeforeEach
    public void setUp() {
        openPath("/catalog");
        catalogPage = new CatalogPage(page);
        materialPage = new MaterialPage(page);
    }

    @DisplayName("Create material in Catalog Test")
    @Test
    public void createMaterialInCatalogTest(){
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

        catalogPage.waitForLoaded();
        // 2️⃣ Відкрити форму створення матеріалу
        materialSpecsPage =
                catalogPage.clickAddItem(MaterialSpecsPage.class);

        // 3️⃣ Заповнити основну інформацію
        materialSpecsPage.setMaterialName(material.name);
        materialSpecsPage.setItemNumber(material.itemNumber);
        materialSpecsPage.setDescription(material.description);
        materialSpecsPage.setBrand(material.brand);
        materialSpecsPage.setManufacturer(material.manufacturer);

        // 4️⃣ Додати варіант
        materialSpecsPage.clickAddMaterialVariantButton();
        materialSpecsPage.setVariantName(material.variationName);
        materialSpecsPage.setVariantDescription(material.variationDescription);

        // 5️⃣ Перейти на сторінку цін
        priceAndVariantsPage = materialSpecsPage.clickNextButton();
        priceAndVariantsPage.setCostForClient(material.costForClient);
        priceAndVariantsPage.setCostForBusiness(material.costForBusiness);

        // 6️⃣ Перейти на сторінку складу
        stockSetupPage = priceAndVariantsPage.clickNextButton();
        stockSetupPage.clickAddLocationButton();
        stockSetupPage.clickChooseLocationButton();

        // Вибрати склад і задати кількість
        stockSetupPage.selectWarehouse(warehouse);
        stockSetupPage.setQuantity(material.quantity);
        stockSetupPage.clickSaveLocationButton();

        // 7️⃣ Зберегти створений матеріал
        stockSetupPage.clickSaveButton();

        AlertUtils.waitForAlertVisible(page);
        String alert = AlertUtils.getAlertText(page);
        Assertions.assertThat(alert).isEqualTo("%s has been successfully added", material.name);
        AlertUtils.waitForAlertHidden(page);

        Assertions.assertThat(catalogPage.getFirstMaterialNameInTheList()).isEqualTo(material.name);
        Assertions.assertThat(catalogPage.getFirstItemNumberInTheList()).isEqualTo(material.itemNumber);

        catalogPage.clickOnTheFirstLocationArrowDownButton();

        Assertions.assertThat(catalogPage.getMaterialLocationInTheDropDown()).isEqualTo(warehouse);
        Assertions.assertThat(catalogPage.getMaterialVariation()).isEqualTo(material.variationName);
        Assertions.assertThat(catalogPage.getQtyInMaterialLocation()).isEqualTo(material.quantity);
    }

    @DisplayName("Update Material in the Catalog")
    @Test
    public void updateMaterialInCatalogTest(){
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

        catalogPage.waitForLoaded();



        catalogPage.openFirstRowThreeDots();
        materialSpecsPage = catalogPage.chooseMenuEditMaterial();

        // 3️⃣ Заповнити основну інформацію
        materialSpecsPage.setMaterialName(editedMaterial.name);
        materialSpecsPage.setItemNumber(editedMaterial.itemNumber);
        materialSpecsPage.setDescription(editedMaterial.description);
        materialSpecsPage.setBrand(editedMaterial.brand);
        materialSpecsPage.setManufacturer(editedMaterial.manufacturer);

        materialSpecsPage.clickSaveButtonInTheEditMaterialFlow();

        Assertions.assertThat(catalogPage.getFirstMaterialNameInTheList()).isEqualTo(editedMaterial.name);
        Assertions.assertThat(catalogPage.getFirstItemNumberInTheList()).isEqualTo(editedMaterial.itemNumber);

    }

    @DisplayName("Delete Material Test")
    @Test
    public void deleteMaterialInCatalogTest(){
        catalogPage.waitForLoaded();

        String firstNameForDeleting = catalogPage.getFirstMaterialNameInTheList();

        catalogPage.openFirstRowThreeDots();
        catalogPage.chooseMenuDeleteMaterial();
        catalogPage.clickDeleteMaterialInConfirmationModalButton();

        Assertions.assertThat(catalogPage.getMaterialNamesList()).doesNotContain(firstNameForDeleting);
    }




}
