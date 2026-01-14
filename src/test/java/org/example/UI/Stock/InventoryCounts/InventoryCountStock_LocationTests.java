package org.example.UI.Stock.InventoryCounts;

import com.microsoft.playwright.assertions.PlaywrightAssertions;
import org.assertj.core.api.Assertions;
import org.example.BaseUITestExtension.PlaywrightUiLoginBaseTest;
import org.example.UI.PageObjectModels.Alerts.AlertUtils;
import org.example.UI.PageObjectModels.Stock.InventoryCount.CreateInventoryCountPage;
import org.example.UI.PageObjectModels.Stock.InventoryCount.InventoryCountListPage;
import org.example.UI.PageObjectModels.Stock.StockPage;
import org.example.UI.PageObjectModels.Stock.Warehouse.WarehousePage;
import org.example.UI.PageObjectModels.Utils.LocationSelect;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.example.domain.LocationName.WAREHOUSE_MAIN;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InventoryCountStock_LocationTests extends PlaywrightUiLoginBaseTest {


    WarehousePage warehousePage;
    InventoryCountListPage inventoryCountListPage;
    CreateInventoryCountPage createInventoryCountPage;
    StockPage stockPage;


    @BeforeEach
    public void setUp() {
        //openPath("/stock");
        warehousePage = new WarehousePage(page);
        stockPage = new StockPage(page);
        inventoryCountListPage = new InventoryCountListPage(page);
    }


    @DisplayName("Create Cycle (Inventory) Count In Stock")
    @Order(0)
    @Test
    public void createInventoryCountTest() {
        openPath("/stock");
        stockPage.clickOnInventoryCountsTab();

        // 2) відкрити форму створення
        createInventoryCountPage = inventoryCountListPage.clickCreateInventoryCountButton();

        LocationSelect.selectLocationStatic(page, WAREHOUSE_MAIN.value());

        // 3) вибрати користувача (react-select)
        String userToAssign = "NAZARII"; // підстав свого користувача
        createInventoryCountPage.assignUserForInventoryCount(userToAssign);

        // 4) встановити дати: сьогодні
        createInventoryCountPage.setStartDateToday();
        createInventoryCountPage.setEndDateToday();

        // 5) встановити Number of days = 10
        createInventoryCountPage.setNumberOfDays(10);

        // 6) Confirm
        createInventoryCountPage.clickConfirm();

        // 7) Перевірка: алерт про успіх (текст може різнитись — перевіряємо, що містить "successfully")
        AlertUtils.waitForAlertVisibleWithToast(page);
        String alertText = AlertUtils.getAlertTextWithToast(page);
        Assertions.assertThat(alertText).contains("All inventory counts were created successfully");
        PlaywrightAssertions.assertThat(
                page.getByRole(com.microsoft.playwright.options.AriaRole.ALERT)
        ).containsText("success");
        AlertUtils.waitForAlertHiddenWithToast(page);

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));

        PlaywrightAssertions.assertThat(inventoryCountListPage.getFirstCycleCountTodayDate()).isVisible();
        Assertions.assertThat(inventoryCountListPage.getFirstCycleCountTodayDateString()).isEqualTo(today);


    }

    @DisplayName("Delete Cycle (Inventory) Count in the Cycle Counts Tab")
    @Order(1)
    @Test
    public void deleteInventoryCountTest() {
        openPath("/stock");
        // 1) перейти на табу Inventory Count
        stockPage.clickOnInventoryCountsTab();

        inventoryCountListPage.clickFirstThreeDotsButton();
        inventoryCountListPage.clickOnDeleteButton();
        inventoryCountListPage.clickOnDeleteButtonInConfirmationModal();

        AlertUtils.waitForAlertVisible(page);
        String alertText = AlertUtils.getAlertText(page);
        Assertions.assertThat(alertText).contains("The item has been successfully deleted");
        PlaywrightAssertions.assertThat(
                page.getByRole(com.microsoft.playwright.options.AriaRole.ALERT)
        ).containsText("success");
        AlertUtils.waitForAlertHidden(page);

    }

    @DisplayName("Create Cycle (Inventory) Count In Location")
    @Order(2)
    @Test
    public void createInventoryCountInWarehouseTest() {
        openPath("/stock/warehouse/warehousemain/ac1f56fd-9919-137e-8199-1f504b6607e8");

        // 1) перейти на табу Inventory Count
        inventoryCountListPage = warehousePage.clickOnInventoryCountTabButton();

        // 2) відкрити форму створення
        createInventoryCountPage = inventoryCountListPage.clickCreateInventoryCountButton();

        PlaywrightAssertions.assertThat(createInventoryCountPage.getTheWarehouseMainLocator()).isVisible();
        LocationSelect.selectLocationStatic(page, WAREHOUSE_MAIN.value());

        // 3) вибрати користувача (react-select)
        String userToAssign = "NAZARII"; // підстав свого користувача
        createInventoryCountPage.assignUserForInventoryCount(userToAssign);

        // 4) встановити дати: сьогодні
        createInventoryCountPage.setStartDateToday();
        createInventoryCountPage.setEndDateToday();

        // 5) встановити Number of days = 10
        createInventoryCountPage.setNumberOfDays(10);

        // 6) Confirm
        createInventoryCountPage.clickConfirm();

        // 7) Перевірка: алерт про успіх (текст може різнитись — перевіряємо, що містить "successfully")
        AlertUtils.waitForAlertVisibleWithToast(page);
        String alertText = AlertUtils.getAlertTextWithToast(page);
        Assertions.assertThat(alertText).contains("All inventory counts were created successfully");
        PlaywrightAssertions.assertThat(
                page.getByRole(com.microsoft.playwright.options.AriaRole.ALERT)
        ).containsText("success");
        AlertUtils.waitForAlertHiddenWithToast(page);

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));

        PlaywrightAssertions.assertThat(inventoryCountListPage.getFirstCycleCountTodayDate()).isVisible();
        Assertions.assertThat(inventoryCountListPage.getFirstCycleCountTodayDateString()).isEqualTo(today);


    }


    @DisplayName("Edit Cycle (Inventory) Count")
    @Order(3)
    @Disabled("The edit logic will be implemented soon")
    @Test
    public void editInventoryCountTest() {
        openPath("/stock/warehouse/warehousemain/ac1f56fd-9919-137e-8199-1f504b6607e8");

        // 1) перейти на табу Inventory Count
        inventoryCountListPage = warehousePage.clickOnInventoryCountTabButton();

        inventoryCountListPage.clickFirstThreeDotsButton();
        inventoryCountListPage.clickOnEditButton();

    }


    @DisplayName("Delete Cycle (Inventory) Count in the Warehouse")
    @Order(4)
    @Test
    public void deleteInventoryCountInTheWarehouseTest() {
        openPath("/stock/warehouse/warehousemain/ac1f56fd-9919-137e-8199-1f504b6607e8");

        // 1) перейти на табу Inventory Count
        inventoryCountListPage = warehousePage.clickOnInventoryCountTabButton();

        inventoryCountListPage.clickFirstThreeDotsButton();
        inventoryCountListPage.clickOnDeleteButton();
        inventoryCountListPage.clickOnDeleteButtonInConfirmationModal();

        AlertUtils.waitForAlertVisible(page);
        String alertText = AlertUtils.getAlertText(page);
        Assertions.assertThat(alertText).isEqualTo("The item has been successfully deleted");
        PlaywrightAssertions.assertThat(
                page.getByRole(com.microsoft.playwright.options.AriaRole.ALERT)
        ).containsText("success");
        AlertUtils.waitForAlertHidden(page);

    }
}
