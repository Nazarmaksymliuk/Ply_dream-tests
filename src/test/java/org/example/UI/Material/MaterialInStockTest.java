package org.example.UI.Material;

import org.assertj.core.api.Assertions;
import org.example.BaseUITestExtension.PlaywrightUiLoginBaseTest;
import org.example.UI.Models.Material;
import org.example.UI.PageObjectModels.Alerts.AlertUtils;
import org.example.UI.PageObjectModels.Material.MaterialEditAvailabilityFlow.MaterialEditAvailabilityPopUpPage;
import org.example.UI.PageObjectModels.Material.MaterialsCreationFlow.MaterialSpecsPage;
import org.example.UI.PageObjectModels.Material.MaterialsCreationFlow.PriceAndVariantsPage;
import org.example.UI.PageObjectModels.Material.MaterialsCreationFlow.MaterialStockSetupPage;
import org.example.UI.PageObjectModels.Material.MaterialsListPage;
import org.example.UI.PageObjectModels.Stock.Warehouse.WarehousePage;
import org.junit.jupiter.api.*;

import java.util.Random;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MaterialInStockTest extends PlaywrightUiLoginBaseTest {
    WarehousePage warehousePage;
    MaterialSpecsPage materialSpecsPage;
    PriceAndVariantsPage priceAndVariantsPage;
    MaterialStockSetupPage stockSetupPage;
    MaterialsListPage materialsListPage;

    private final String defaultVariation = "Single";
    private final String defaultUnitOfMeasurement = "Ea";

    @BeforeEach
    public void setUp() {
        // йдемо одразу в головний склад
        openPath("/stock/warehouse/warehousemain/ac1f56fd-9919-137e-8199-1f504b6607e8");
        warehousePage = new WarehousePage(page);
        materialsListPage = new MaterialsListPage(page);
        warehousePage.waitForLoaded();
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

    @DisplayName("Create material from Stock (WarehouseMain)")
    @Order(0)
    @Test
    public void createMaterialFromStockTest() {

        // 1) Стартуємо флоу створення матеріалу зі складу
        materialSpecsPage = warehousePage.clickOnAddNewMaterialButton();

        // 2) Специфікація
        materialSpecsPage.setMaterialName(material.name);
        materialSpecsPage.setItemNumber(material.itemNumber);
        materialSpecsPage.setDescription(material.description);
        materialSpecsPage.setBrand(material.brand);
        materialSpecsPage.setManufacturer(material.manufacturer);

        // noo needs now
        //materialSpecsPage.clickAddMaterialVariantButton();
        materialSpecsPage.setVariantName(material.variationName);
        materialSpecsPage.setVariantDescription(material.variationDescription);

        // 3) Ціни
        priceAndVariantsPage = materialSpecsPage.clickNextButton();
        priceAndVariantsPage.setCostForClient(material.costForClient);
        priceAndVariantsPage.setCostForBusiness(material.costForBusiness);

        // 4) Stock Setup
        // У ВWarehouseMain **склад уже вибрано автоматично**, тому:
        // - НЕ клікаємо Add Location / Choose Location / selectWarehouse
        stockSetupPage = priceAndVariantsPage.clickNextButton();
        stockSetupPage.clickAddLocationButton();
        stockSetupPage.setQuantity(material.quantity);
        stockSetupPage.clickSaveLocationButton();
        stockSetupPage.clickSaveButton();

        // 5) Перевірки
        AlertUtils.waitForAlertVisible(page);
        String alert = AlertUtils.getAlertText(page);
        // текст може відрізнятися від каталогу: робимо перевірку «містить»
        Assertions.assertThat(alert).contains("successfully");
        AlertUtils.waitForAlertHidden(page);

        // Список матеріалів у межах цього складу
        Assertions.assertThat(materialsListPage.getFirstMaterialNameInTheList()).isEqualTo(material.name);
        Assertions.assertThat(materialsListPage.getFirstItemNumberInTheList()).isEqualTo(material.itemNumber);

        // Якщо у списку на складі є дропдаун локацій — можна розкрити і звірити кількість:
        // materialsListPage.clickFirstLocationArrowDown();
        // Assertions.assertThat(materialsListPage.getQtyFromMaterialLocation()).isEqualTo(material.quantity);
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
    @DisplayName("Update Material in Stock (WarehouseMain)")
    @Order(1)
    @Test
    public void updateMaterialAvailabilityInStockTest(){
        // знімаємо поточне значення кількості з першого рядка
        int beforeQty = materialsListPage.getFirstRowQuantity();

        // відкриваємо "..." → Edit availability
        materialsListPage.openFirstRowMaterialStockThreeDots();
        materialsListPage.chooseMenuEditMaterialAvailability();

        // працюємо в попапі
        MaterialEditAvailabilityPopUpPage popup = new MaterialEditAvailabilityPopUpPage(page);
        popup.waitForLoaded();

        int newQty = 100;  // будь-яке коректне нове значення
        int newMin = 50;
        int newMax = 150;

        popup.setQuantity(newQty);
        popup.setMinAmount(newMin);
        popup.setMaxAmount(newMax);
        popup.clickSaveChanges();

        AlertUtils.waitForAlertVisible(page);
        String alert = AlertUtils.getAlertText(page);
        // текст може різнитись → просто contains
        Assertions.assertThat(alert).contains("successfully");
        AlertUtils.waitForAlertHidden(page);

        // перевіряємо, що кількість у гріді оновилася
        int afterQty = materialsListPage.getFirstRowQuantity();
        Assertions.assertThat(afterQty)
                .as("Quantity in grid should be updated")
                .isEqualTo(newQty);

    }

    @DisplayName("Delete Material in Stock (WarehouseMain)")
    @Order(2)
    @Test
    public void deleteMaterialInStockTest(){
        String firstNameForDeleting = materialsListPage.getFirstMaterialNameInTheList();

        materialsListPage.openFirstRowMaterialStockThreeDots();
        materialsListPage.chooseMenuDeleteMaterial();
        materialsListPage.confirmDeleteMaterialInModal();

        AlertUtils.waitForAlertVisible(page);
        String alert = AlertUtils.getAlertText(page);
        // текст в stock може відрізнятись — перевіримо по contains
        Assertions.assertThat(alert).contains("deleted");
        AlertUtils.waitForAlertHidden(page);

        waitForElementRemoved(firstNameForDeleting);
        Assertions.assertThat(materialsListPage.getMaterialNamesList()).doesNotContain(firstNameForDeleting);
    }

}
