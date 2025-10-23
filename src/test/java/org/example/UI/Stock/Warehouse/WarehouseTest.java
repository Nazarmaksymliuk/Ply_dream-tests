package org.example.UI.Stock.Warehouse;

import com.microsoft.playwright.assertions.PlaywrightAssertions;
import net.datafaker.Faker;
import org.assertj.core.api.Assertions;
import org.example.BaseTestExtension.PlaywrightBaseTest;
import org.example.Models.Warehouse;
import org.example.PageObjectModels.Stock.StockPage;
import org.example.PageObjectModels.Stock.Warehouse.CreateWarehousePopUpPage;
import org.example.PageObjectModels.Stock.Warehouse.EditWarehousePopUpPage;
import org.example.PageObjectModels.Stock.Warehouse.WarehousesListPage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class WarehouseTest extends PlaywrightBaseTest {
    StockPage stockPage;
    WarehousesListPage warehousesListPage;
    CreateWarehousePopUpPage createNewLocationPopUpPage;
    EditWarehousePopUpPage editWarehousePopUpPage;
    @BeforeAll
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
    @Test
    public void createWarehouseTest() {
        PlaywrightAssertions.assertThat(stockPage.warehousesTabButton()).isVisible();

        createNewLocationPopUpPage = warehousesListPage.clickOnAddWarehouseButton();


        createNewLocationPopUpPage.setWarehouseName(warehouse.name);
        createNewLocationPopUpPage.setWarehouseAddress(warehouse.address);
        createNewLocationPopUpPage.setWarehouseCity(warehouse.city);

        createNewLocationPopUpPage.chooseFirstWarehouseState();

        createNewLocationPopUpPage.setWarehouseApt(warehouse.apt);
        createNewLocationPopUpPage.setWarehouseZip(warehouse.zip);

        createNewLocationPopUpPage.clickAddWarehouseButton();

        waitForElementPresent(warehouse.name);
        Assertions.assertThat(warehousesListPage.getWarehousesNamesList()).contains(warehouse.name);
        Assertions.assertThat(warehousesListPage.getWarehousesAddressList()).contains(
                warehouse.address, warehouse.city, warehouse.zip, warehouse.apt
        );

    }
    Warehouse editedWarehouse = new Warehouse(
            "Warehouse-" + new Random().nextInt(100000),
            faker.address().streetAddress(),
            faker.address().city(),
            faker.address().zipCode(),
            "Apt.1A"
    );
    @DisplayName("Update Warehouse Test")
    @Test
    public void updateWarehouseTest() {
        PlaywrightAssertions.assertThat(stockPage.warehousesTabButton()).isVisible();

        warehousesListPage.clickOnWarehouseThreeDotsButton();
        editWarehousePopUpPage = warehousesListPage.clickOnEditWarehouseButton();

        String oldName = warehousesListPage.getFirstWarehouseName();

        editWarehousePopUpPage.waitForLoaded();
        editWarehousePopUpPage.setWarehouseName(editedWarehouse.name);
        editWarehousePopUpPage.setWarehouseAddress(editedWarehouse.address);
        editWarehousePopUpPage.setWarehouseCity(editedWarehouse.city);
        editWarehousePopUpPage.setWarehouseZip(editedWarehouse.zip);
        editWarehousePopUpPage.setWarehouseApt(editedWarehouse.apt);
        editWarehousePopUpPage.setWarehouseState(editedWarehouse.state);

        waitForElementPresent(editedWarehouse.name);
        Assertions.assertThat(warehousesListPage.getWarehousesNamesList())
                .contains(editedWarehouse.name);
        Assertions.assertThat(warehousesListPage.getWarehousesAddressList())
                .anySatisfy(addr -> {
                    Assertions.assertThat(addr).contains(editedWarehouse.address);
                    Assertions.assertThat(addr).contains(editedWarehouse.city);
                    Assertions.assertThat(addr).contains(editedWarehouse.zip);
                    Assertions.assertThat(addr).contains(editedWarehouse.apt);
                });
    }

    @DisplayName("Delete Warehouse Test")
    @Test
    public void deleteWarehouseTest() {
        PlaywrightAssertions.assertThat(stockPage.warehousesTabButton()).isVisible();

        String firstWarehouseName = warehousesListPage.getFirstWarehouseName();

        warehousesListPage.clickOnWarehouseThreeDotsButton();
        warehousesListPage.clickOnDeleteButton();
        warehousesListPage.clickDeleteWarehouseInConfirmationModalButton();

        waitForElementPresent(firstWarehouseName);
        Assertions.assertThat(warehousesListPage.getWarehousesNamesList()).doesNotContain(firstWarehouseName);
    }
}
