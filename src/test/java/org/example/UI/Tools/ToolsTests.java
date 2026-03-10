package org.example.UI.Tools;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import net.datafaker.Faker;
import org.assertj.core.api.Assertions;
import org.example.BaseUIApiExtension.PlaywrightUiApiBaseTest;
import org.example.config.TestEnvironment;
import org.example.UI.Models.Tool;
import org.example.UI.Models.ToolUnit;
import org.example.UI.PageObjectModels.Alerts.AlertUtils;
import org.example.UI.PageObjectModels.Catalog.CatalogPage;
import org.example.UI.PageObjectModels.Tools.ToolsCreationFlow.AddEditUnitsPage;
import org.example.UI.PageObjectModels.Tools.ToolsCreationFlow.ToolGeneralInformationPage;
import org.example.UI.PageObjectModels.Tools.ToolsListPage;
import org.example.fixtures.WarehouseApiFixture;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.example.domain.LocationName.WAREHOUSE_MAIN;

@Epic("Tools")
@Feature("Tools UI CRUD")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ToolsTests extends PlaywrightUiApiBaseTest {
    CatalogPage catalogPage;
    ToolsListPage toolsListPage;
    ToolGeneralInformationPage generalInformationPage;
    AddEditUnitsPage addUnitsPage;

    private static WarehouseApiFixture warehouseFixture;

    private static String warehouseId;
    private static String warehouseName;

    private final Faker faker = new Faker();

    @BeforeEach
    public void setUp() {
        openPath("/catalog");
        catalogPage = new CatalogPage(page);
        toolsListPage = new ToolsListPage(page);
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

    Tool tool = new Tool(
            "Tool-" + new Faker().number().numberBetween(10000, 99999),
            "MFG-" + new Faker().number().numberBetween(10000, 99999),
            "High-performance tool for any type of work.",
            "test tag"
    );

    ToolUnit toolUnit = new ToolUnit(
            "Impact Driver Unit-" + new Faker().number().numberBetween(10000, 99999),
            "SerialNumber-" + new Faker().number().numberBetween(10000, 99999),
            WAREHOUSE_MAIN.value(),
            250.00,
            400.00
    );

    @DisplayName("Create Tool with Unit in the Catalog Test")
    @Order(0)
    @Test
    public void testCreateTool() {

        catalogPage.waitForLoaded();

        catalogPage.openToolsTab();

        generalInformationPage = catalogPage.clickAddItem(ToolGeneralInformationPage.class);
        generalInformationPage.setToolName(tool.name);
        generalInformationPage.setMfgNumber(tool.mfgNumber);
        generalInformationPage.setToolDescription(tool.description);
        generalInformationPage.setTags(tool.tags);

        addUnitsPage = generalInformationPage.clickNextPage();

        addUnitsPage.setUnitName(toolUnit.unitName);
        addUnitsPage.setSerialNumber(toolUnit.serialNumber);
        addUnitsPage.selectFirstStatus();
        addUnitsPage.setWarehouseWithoutUtility(warehouseName);
        addUnitsPage.setPurchaseCost(toolUnit.purchaseCost);
        addUnitsPage.setValue(toolUnit.unitValue);

        addUnitsPage.clickSaveInformationButton();

        addUnitsPage.clickSaveButton();

        AlertUtils.waitForAlertVisible(page);
        String alert = AlertUtils.getAlertText(page);
        Assertions.assertThat(alert).isEqualTo("Tool \"%s\" has been successfully created", tool.name);
        AlertUtils.waitForAlertHidden(page);

        Assertions.assertThat(toolsListPage.getFirstToolNameInTheList()).isEqualTo(tool.name);
        Assertions.assertThat(toolsListPage.getFirstUnitNameInTheList()).isEqualTo(toolUnit.unitName);
        Assertions.assertThat(toolsListPage.getFirstToolMFGInTheList()).isEqualTo(toolUnit.serialNumber);
        Assertions.assertThat(toolsListPage.getFirstToolUnitWarehouseLocationInTheList()).isEqualTo(warehouseName);
        Assertions.assertThat(toolsListPage.getFirstToolStatusLocationInTheList()).isEqualTo("Available");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        LocalDate actualDate = LocalDate.parse(toolsListPage.getFirstToolDateInTheList(), formatter);
        LocalDate today = LocalDate.now();
        Assertions.assertThat(actualDate)
                .as("Start date should be today's date")
                .isEqualTo(today);
    }

    ToolUnit editedToolUnit = new ToolUnit(
            "Impact Driver Unit A",
            "SN-DRILL-12345",
            WAREHOUSE_MAIN.value(),
            250.00,
            400.00
    );

    @DisplayName("Edit Tool Unit in the Catalog Test")
    @Order(1)
    @Test
    public void testEditToolUnit() {
        catalogPage.waitForLoaded();
        catalogPage.openToolsTab();
        catalogPage.openFirstRowToolThreeDots();

        String toolName = toolsListPage.getFirstToolNameInTheList();

        addUnitsPage = catalogPage.chooseMenuEditToolUnit();

        addUnitsPage.setUnitName(toolUnit.unitName);
        addUnitsPage.setSerialNumber(toolUnit.serialNumber);
        addUnitsPage.setPurchaseCost(toolUnit.purchaseCost);
        addUnitsPage.setValue(toolUnit.unitValue);

        addUnitsPage.clickSaveInformationButton();

        AlertUtils.waitForAlertVisible(page);
        page.waitForTimeout(TestEnvironment.DROPDOWN_DELAY_MS);
        String alert = AlertUtils.getAlertText(page);
        Assertions.assertThat(alert).isEqualTo("Unit updated successfully");
        AlertUtils.waitForAlertHidden(page);

        Assertions.assertThat(toolsListPage.getFirstToolNameInTheList()).isEqualTo(toolName);
        Assertions.assertThat(toolsListPage.getFirstUnitNameInTheList()).isEqualTo(toolUnit.unitName);
        Assertions.assertThat(toolsListPage.getFirstToolMFGInTheList()).isEqualTo(toolUnit.serialNumber);
        Assertions.assertThat(toolsListPage.getFirstToolUnitWarehouseLocationInTheList()).isEqualTo(warehouseName);
        Assertions.assertThat(toolsListPage.getFirstToolStatusLocationInTheList()).isEqualTo("Available");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        LocalDate actualDate = LocalDate.parse(toolsListPage.getFirstToolDateInTheList(), formatter);
        LocalDate today = LocalDate.now();
        Assertions.assertThat(actualDate)
                .as("Start date should be today's date")
                .isEqualTo(today);
    }

    @DisplayName("Delete Tool Test")
    @Order(2)
    @Test
    public void testDeleteTool() {
        catalogPage.waitForLoaded();
        catalogPage.openToolsTab();

        String toolName = toolsListPage.getFirstUnitNameInTheList();

        catalogPage.openFirstRowToolThreeDots();
        catalogPage.chooseMenuDeleteTool();
        catalogPage.confirmDeleteItemInModal();

        AlertUtils.waitForAlertVisible(page);
        page.waitForTimeout(TestEnvironment.DROPDOWN_DELAY_MS);
        String alert = AlertUtils.getAlertText(page);
        Assertions.assertThat(alert).isEqualTo("Tool deleted successfully");
        AlertUtils.waitForAlertHidden(page);

        waitForElementRemoved(toolName);
        Assertions.assertThat(toolsListPage.getToolNamesList()).doesNotContain(toolName);
    }
}
