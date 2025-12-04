package org.example.UI.Stock.Warehouse;

import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import net.datafaker.Faker;
import org.assertj.core.api.Assertions;
import org.example.BaseUITestExtension.PlaywrightUiLoginBaseTest;
import org.example.UI.Models.Warehouse;
import org.example.UI.PageObjectModels.Alerts.AlertUtils;
import org.example.UI.PageObjectModels.Stock.StockPage;
import org.example.UI.PageObjectModels.Stock.Warehouse.CreateWarehousePopUpPage;
import org.example.UI.PageObjectModels.Stock.Warehouse.EditWarehousePopUpPage;
import org.example.UI.PageObjectModels.Stock.Warehouse.WarehousesListPage;
import org.junit.jupiter.api.*;

import java.util.Random;
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WarehouseTest extends PlaywrightUiLoginBaseTest {
    StockPage stockPage;
    WarehousesListPage warehousesListPage;
    CreateWarehousePopUpPage createNewLocationPopUpPage;
    EditWarehousePopUpPage editWarehousePopUpPage;

    @BeforeEach
    public void setUp() {
        openPath("/stock");
        stockPage = new StockPage(page);
        warehousesListPage = new WarehousesListPage(page);
    }

    Faker faker = new Faker();
    Warehouse warehouse = new Warehouse(
            "Warehouse-" + new Random().nextInt(100000),
            faker.address().streetAddress(),
            faker.address().city(),
            faker.address().zipCode(),
            "Apt.1A"
    );

    @DisplayName("Create Warehouse Test")
    @Order(0)
    @Test
    public void createWarehouseTest() {
        PlaywrightAssertions.assertThat(stockPage.warehousesTabButton())
                .isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(20_000));

        createNewLocationPopUpPage = warehousesListPage.clickOnAddWarehouseButton();


        createNewLocationPopUpPage.setWarehouseName(warehouse.name);
        createNewLocationPopUpPage.setWarehouseAddress(warehouse.address);
        createNewLocationPopUpPage.setWarehouseCity(warehouse.city);

        createNewLocationPopUpPage.setFirstStateForLocation();

        createNewLocationPopUpPage.setWarehouseApt(warehouse.apt);
        createNewLocationPopUpPage.setWarehouseZip(warehouse.zip);

        createNewLocationPopUpPage.clickAddWarehouseButton();

        AlertUtils.waitForAlertVisible(page);
        String alert = AlertUtils.getAlertText(page);
        Assertions.assertThat(alert).isEqualTo("Location has been created");
        AlertUtils.waitForAlertHidden(page);

        waitForElementPresent(warehouse.name);
        Assertions.assertThat(warehousesListPage.getWarehousesNamesList()).contains(warehouse.name);
        Assertions.assertThat(warehousesListPage.getWarehousesAddressList())
                .anyMatch(addr -> addr.contains(warehouse.address)
                && addr.contains(warehouse.city)
                && addr.contains(warehouse.zip)
                && addr.contains(warehouse.apt));

    }


    Warehouse editedWarehouse = new Warehouse(
            "Warehouse-" + new Random().nextInt(100000),
            faker.address().streetAddress(),
            faker.address().city(),
            faker.address().zipCode(),
            "Apt.1A"
    );
    @DisplayName("Update Warehouse Test")
    @Order(1)
    @Test
    public void updateWarehouseTest() {
        waitForDomLoaded();
        PlaywrightAssertions.assertThat(stockPage.warehousesTabButton()).isVisible();

        warehousesListPage.clickOnWarehouseThreeDotsButton();
        editWarehousePopUpPage = stockPage.chooseMenuActionEdit(EditWarehousePopUpPage.class);

        String oldName = warehousesListPage.getFirstWarehouseName();

        editWarehousePopUpPage.waitForLoaded();
        editWarehousePopUpPage.setWarehouseName(editedWarehouse.name);
        editWarehousePopUpPage.setWarehouseAddress(editedWarehouse.address);
        editWarehousePopUpPage.setWarehouseCity(editedWarehouse.city);
        editWarehousePopUpPage.setWarehouseZip(editedWarehouse.zip);
        editWarehousePopUpPage.setFirstStateForLocation();
        editWarehousePopUpPage.setWarehouseApt(editedWarehouse.apt);

        editWarehousePopUpPage.clickSaveChangesButton();

        AlertUtils.waitForAlertVisible(page);
        String alert = AlertUtils.getAlertText(page);
        Assertions.assertThat(alert).isEqualTo("Location successfully updated");
        AlertUtils.waitForAlertHidden(page);


        waitForElementPresent(editedWarehouse.name);
        Assertions.assertThat(warehousesListPage.getWarehousesNamesList())
                .contains(editedWarehouse.name);
        Assertions.assertThat(warehousesListPage.getWarehousesAddressList())
                .anyMatch(addr -> addr.contains(editedWarehouse.address)
                        && addr.contains(editedWarehouse.city)
                        && addr.contains(editedWarehouse.zip)
                        && addr.contains(editedWarehouse.apt));
    }

    @DisplayName("Delete Warehouse Test")
    @Order(2)
    @Test
    public void deleteWarehouseTest() {
        PlaywrightAssertions.assertThat(stockPage.warehousesTabButton()).isVisible();

        String firstWarehouseName = warehousesListPage.getFirstWarehouseName();

        // Перед видаленням
        if ("WarehouseMain".equals(firstWarehouseName)) {
            System.out.println("⚠️ WarehouseMain could not be deleted");
            Assumptions.assumeTrue(false, "WarehouseMain — could not be deleted");
        }

        warehousesListPage.clickOnWarehouseThreeDotsButton();
        warehousesListPage.clickOnDeleteButton();
        warehousesListPage.clickDeleteWarehouseInConfirmationModalButton();

        AlertUtils.waitForAlertVisible(page);
        String alert = AlertUtils.getAlertText(page);
        Assertions.assertThat(alert).isEqualTo("\"%s\" deleted successfully", firstWarehouseName);
        AlertUtils.waitForAlertHidden(page);

        waitForElementPresent(firstWarehouseName);
        Assertions.assertThat(warehousesListPage.getWarehousesNamesList()).doesNotContain(firstWarehouseName);
    }
}
