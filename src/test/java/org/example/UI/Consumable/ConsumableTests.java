package org.example.UI.Consumable;

import org.assertj.core.api.Assertions;
import org.example.BaseUIApiExtension.PlaywrightUiApiBaseTest;
import org.example.UI.Models.Consumable;
import org.example.UI.PageObjectModels.Alerts.AlertUtils;
import org.example.UI.PageObjectModels.Catalog.CatalogPage;
import org.example.UI.PageObjectModels.Consumable.ConsumableCreationFlow.ConsumableGeneralInfoPage;
import org.example.UI.PageObjectModels.Consumable.ConsumableCreationFlow.ConsumableStockSetupPage;
import org.example.UI.PageObjectModels.Consumable.ConsumableListPage;
import org.example.UI.PageObjectModels.Stock.Warehouse.WarehousePage;
import org.example.fixtures.WarehouseApiFixture;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.Random;

import static org.example.domain.LocationName.WAREHOUSE_MAIN;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ConsumableTests extends PlaywrightUiApiBaseTest {
    CatalogPage catalogPage;
    ConsumableGeneralInfoPage consumableGeneralInfoPage;
    ConsumableStockSetupPage consumableStockSetupPage;
    ConsumableListPage consumableListPage;
    WarehousePage warehousePage;

    private static WarehouseApiFixture warehouseFixture;

    private static String warehouseId;
    private static String warehouseName;

    @BeforeEach
    public void setUp() {
        openPath("/catalog");
        catalogPage = new CatalogPage(page);
        consumableListPage = new ConsumableListPage(page);
        warehousePage = new WarehousePage(page);
    }

    @BeforeAll
    void createWarehouse() throws IOException {
        warehouseFixture = WarehouseApiFixture.create(userApi)
                .provisionWarehouse("UI-CATALOG-E2E ");

        warehouseId = warehouseFixture.warehouseId();
        warehouseName = warehouseFixture.warehouseName();
    }

    @AfterAll
    void deleteWarehouse() {
        if (warehouseFixture != null) {
            warehouseFixture.cleanup("Cleanup after MaterialInCatalogTest");
        }
    }

    Consumable consumable = new Consumable(
            "Consumable-" + new Random().nextInt(100000),          // name
            "P-" + new Random().nextInt(100000),                  // itemNumber
            "A4 size, 80gsm",         // description
            "pack",                   // unitOfMeasurement
            5.75,                    // costForBusiness
            10.1,
            WAREHOUSE_MAIN.value(),
            "test-tag"
    );

    Consumable editedConsumable = new Consumable(
            "Consumable-edited-" + new Random().nextInt(100000),          // name
            "P-edited-" + new Random().nextInt(100000),                  // itemNumber
            "A4 size, 80gsm edited",         // description
            "pack",                   // unitOfMeasurement
            10.75,                    // costForBusiness
            10.1,
            WAREHOUSE_MAIN.value(),
            "test-tag-edited"
    );

    @DisplayName("Create Consumable Test")
    @Order(0)
    @Test
    public void testCreateConsumable(){
        catalogPage.waitForLoaded();

        catalogPage.openConsumableTab();

        consumableGeneralInfoPage = catalogPage.clickAddItem(ConsumableGeneralInfoPage.class);

        consumableGeneralInfoPage.setName(consumable.name);
        consumableGeneralInfoPage.setItemNumber(consumable.itemNumber);
        consumableGeneralInfoPage.setDescription(consumable.description);
        consumableGeneralInfoPage.setCostForBusiness(consumable.costForBusiness);
        consumableGeneralInfoPage.setTag(consumable.tag);

        consumableStockSetupPage = consumableGeneralInfoPage.clickNextButton();

        consumableStockSetupPage.clickAddWarehouseButton();
        consumableStockSetupPage.setWarehouseUsingUtility(warehouseName);
        consumableStockSetupPage.setQuantity(consumable.quantity);

        consumableStockSetupPage.clickSaveWarehouseButton();

        consumableStockSetupPage.clickSaveButton();


        AlertUtils.waitForAlertVisible(page);
        String alert = AlertUtils.getAlertText(page);
        Assertions.assertThat(alert).isEqualTo("\"%s\" has been successfully created", consumable.name);
        AlertUtils.waitForAlertHidden(page);

        Assertions.assertThat(consumableListPage.getFirstConsumableNameInTheList()).isEqualTo(consumable.name);
        Assertions.assertThat(consumableListPage.getTheConsumablePrice()).isEqualTo(consumable.costForBusiness);


    }

    @DisplayName("Update Consumable Test")
    @Order(1)
    @Test
    public void testUpdateConsumable(){
        catalogPage.waitForLoaded();
        catalogPage.openConsumableTab();

        catalogPage.openFirstRowConsumableThreeDots();

        consumableGeneralInfoPage = catalogPage.chooseMenuEditConsumable();

        consumableGeneralInfoPage.setName(editedConsumable.name);
        consumableGeneralInfoPage.setItemNumber(editedConsumable.itemNumber);
        consumableGeneralInfoPage.setDescription(editedConsumable.description);
        consumableGeneralInfoPage.setCostForBusiness(editedConsumable.costForBusiness);
        consumableGeneralInfoPage.setTag(editedConsumable.tag);
        consumableGeneralInfoPage.clickSaveButton();

        AlertUtils.waitForAlertVisible(page);
        page.waitForTimeout(2000);
        String alert = AlertUtils.getAlertText(page);
        Assertions.assertThat(alert).isEqualTo("\"%s\" has been successfully updated",editedConsumable.name);
        AlertUtils.waitForAlertHidden(page);

        waitForElementPresent(editedConsumable.name);
        Assertions.assertThat(consumableListPage.getFirstConsumableNameInTheList()).isEqualTo(editedConsumable.name);
        Assertions.assertThat(consumableListPage.getTheConsumablePrice()).isEqualTo(editedConsumable.costForBusiness);

    }

    @DisplayName("Delete Consumable Test")
    @Order(2)
    @Test
    public void testDeleteConsumable() {
        catalogPage.waitForLoaded();
        catalogPage.openConsumableTab();

        String consumableName = consumableListPage.getFirstConsumableNameInTheList();

        catalogPage.openFirstRowConsumableThreeDots();
        catalogPage.chooseMenuActionDelete();
        catalogPage.confirmDeleteItemInModal();

        AlertUtils.waitForAlertVisible(page);
        page.waitForTimeout(1000);
        String alert = AlertUtils.getAlertText(page);
        Assertions.assertThat(alert).isEqualTo("Consumable has been successfully deleted");
        AlertUtils.waitForAlertHidden(page);

        waitForElementRemoved(consumableName);
        Assertions.assertThat(consumableListPage.getConsumableNamesList()).doesNotContain(consumableName);

    }

    @DisplayName("Create Consumable In Location Test")
    @Order(3)
    @Test
    public void testCreateConsumableInLocation(){
        openPath("/stock/warehouse/" + warehouseName.toLowerCase() + "/" + warehouseId);

        warehousePage.waitForLoaded();
        warehousePage.clickOnConsumablesTabButton();
        consumableGeneralInfoPage = warehousePage.clickAddConsumable();

        consumableGeneralInfoPage.setName(consumable.name);
        consumableGeneralInfoPage.setItemNumber(consumable.itemNumber);
        consumableGeneralInfoPage.setDescription(consumable.description);
        consumableGeneralInfoPage.setCostForBusiness(consumable.costForBusiness);
        consumableGeneralInfoPage.setTag(consumable.tag);

        consumableStockSetupPage = consumableGeneralInfoPage.clickNextButton();

        consumableStockSetupPage.clickAddWarehouseButton();
        consumableStockSetupPage.setWarehouseUsingUtility(warehouseName);
        consumableStockSetupPage.setQuantity(consumable.quantity);

        consumableStockSetupPage.clickSaveWarehouseButton();

        consumableStockSetupPage.clickSaveButton();


        AlertUtils.waitForAlertVisible(page);
        String alert = AlertUtils.getAlertText(page);
        Assertions.assertThat(alert).isEqualTo("\"%s\" has been successfully created", consumable.name);
        AlertUtils.waitForAlertHidden(page);

        Assertions.assertThat(consumableListPage.getFirstConsumableNameInTheList()).isEqualTo(consumable.name);

    }

    @DisplayName("Update Consumable in Location Test")
    @Order(4)
    @Test
    public void testUpdateConsumableInLocation(){
        openPath("/stock/warehouse/" + warehouseName.toLowerCase() + "/" + warehouseId);

        warehousePage.waitForLoaded();
        warehousePage.clickOnConsumablesTabButton();

        warehousePage.openFirstRowThreeDots();

        consumableGeneralInfoPage = warehousePage.clickOnEditConsumableButton();

        consumableGeneralInfoPage.setName(editedConsumable.name);
        consumableGeneralInfoPage.setItemNumber(editedConsumable.itemNumber);
        consumableGeneralInfoPage.setDescription(editedConsumable.description);
        consumableGeneralInfoPage.setCostForBusiness(editedConsumable.costForBusiness);
        consumableGeneralInfoPage.setTag(editedConsumable.tag);
        consumableGeneralInfoPage.clickSaveButton();

        AlertUtils.waitForAlertVisible(page);
        page.waitForTimeout(2000);
        String alert = AlertUtils.getAlertText(page);
        Assertions.assertThat(alert).isEqualTo("\"%s\" has been successfully updated",editedConsumable.name);
        AlertUtils.waitForAlertHidden(page);

        waitForElementPresent(editedConsumable.name);
        Assertions.assertThat(consumableListPage.getFirstConsumableNameInTheList()).isEqualTo(editedConsumable.name);
        //Assertions.assertThat(consumableListPage.getTheConsumablePrice()).isEqualTo(editedConsumable.costForBusiness);

    }

    @DisplayName("Delete Consumable In Location Test")
    @Order(2)
    @Test
    public void testDeleteConsumableInLocation() {
        openPath("/stock/warehouse/" + warehouseName.toLowerCase() + "/" + warehouseId);

        warehousePage.waitForLoaded();
        warehousePage.clickOnConsumablesTabButton();

        String consumableName = consumableListPage.getFirstConsumableNameInTheList();

        warehousePage.openFirstRowThreeDots();
        warehousePage.clickOnDeleteButton();
        warehousePage.confirmDeletion();

        AlertUtils.waitForAlertVisible(page);
        page.waitForTimeout(1000);
        String alert = AlertUtils.getAlertText(page);
        Assertions.assertThat(alert).isEqualTo("Consumable has been successfully deleted");
        AlertUtils.waitForAlertHidden(page);

        waitForElementRemoved(consumableName);
        Assertions.assertThat(consumableListPage.getConsumableNamesList()).doesNotContain(consumableName);

    }

}
