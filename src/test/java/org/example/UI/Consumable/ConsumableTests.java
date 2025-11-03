package org.example.UI.Consumable;

import junit.framework.TestCase;
import org.assertj.core.api.Assertions;
import org.example.BaseTestExtension.PlaywrightUiLoginBaseTest;
import org.example.Models.Consumable;
import org.example.PageObjectModels.Alerts.AlertUtils;
import org.example.PageObjectModels.Catalog.CatalogPage;
import org.example.PageObjectModels.Consumable.ConsumableCreationFlow.ConsumableGeneralInfoPage;
import org.example.PageObjectModels.Consumable.ConsumableCreationFlow.ConsumableStockSetupPage;
import org.example.PageObjectModels.Consumable.ConsumableListPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class ConsumableTests extends PlaywrightUiLoginBaseTest {
    CatalogPage catalogPage;
    ConsumableGeneralInfoPage consumableGeneralInfoPage;
    ConsumableStockSetupPage consumableStockSetupPage;
    ConsumableListPage consumableListPage;
    @BeforeEach
    public void setUp() {
        openPath("/catalog");
        catalogPage = new CatalogPage(page);
        consumableListPage = new ConsumableListPage(page);
    }
    Consumable consumable = new Consumable(
            "Consumable-" + new Random().nextInt(100000),          // name
            "P-" + new Random().nextInt(100000),                  // itemNumber
            "A4 size, 80gsm",         // description
            "pack",                   // unitOfMeasurement
            5.75,                    // costForBusiness
            10.1,
            "WarehouseMain",
            "test-tag"
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
        consumableStockSetupPage.setWarehouseUsingUtility(consumable.location);
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


    Consumable editedConsumable = new Consumable(
            "Consumable-edited-" + new Random().nextInt(100000),          // name
            "P-edited-" + new Random().nextInt(100000),                  // itemNumber
            "A4 size, 80gsm edited",         // description
            "pack",                   // unitOfMeasurement
            10.75,                    // costForBusiness
            10.1,
            "WarehouseMain",
            "test-tag-edited"
    );
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

}
