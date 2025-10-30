package org.example.UI.Tools;

import org.assertj.core.api.Assertions;
import org.example.BaseTestExtension.PlaywrightBaseTest;
import org.example.BaseTestExtension.PlaywrightUiLoginBaseTest;
import org.example.Models.Tool;
import org.example.Models.ToolUnit;
import org.example.PageObjectModels.Alerts.AlertUtils;
import org.example.PageObjectModels.Catalog.CatalogPage;
import org.example.PageObjectModels.Tools.ToolsCreationFlow.AddEditUnitsPage;
import org.example.PageObjectModels.Tools.ToolsCreationFlow.ToolGeneralInformationPage;
import org.example.PageObjectModels.Tools.ToolsListPage;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ToolsTests extends PlaywrightUiLoginBaseTest {
    CatalogPage catalogPage;
    ToolsListPage toolsListPage;
    ToolGeneralInformationPage generalInformationPage;
    AddEditUnitsPage addUnitsPage;

    @BeforeEach
    public void setUp() {
        openPath("/catalog");
        catalogPage = new CatalogPage(page);
        toolsListPage = new ToolsListPage(page);
    }

    Tool tool = new Tool(
            "Tool-" + new Random().nextInt(100000),
            "MFG-" + new Random().nextInt(100000),
            "High-performance tool for any type of work.",
            "test tag"
    );

    ToolUnit toolUnit = new ToolUnit(
            "Impact Driver Unit-" + new Random().nextInt(100000) ,
            "SerialNumber-" + new Random().nextInt(100000),
            "WarehouseMain",
            250.00,
            400.00
    );

    @DisplayName("Create Tool in the Catalog Test")
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
        addUnitsPage.setWarehouseWithoutUtility(toolUnit.location);
        addUnitsPage.setPurchaseCost(toolUnit.purchaseCost);
        addUnitsPage.setValue(toolUnit.unitValue);

        addUnitsPage.clickSaveInformationButton();

        addUnitsPage.clickSaveButton();

        AlertUtils.waitForAlertVisible(page);
        String alert = AlertUtils.getAlertText(page);
        Assertions.assertThat(alert).isEqualTo("Tool \"%s\" has been successfully created", tool.name);
        AlertUtils.waitForAlertHidden(page);

        //waitForElementPresent(tool.name);
        Assertions.assertThat(toolsListPage.getFirstToolNameInTheList()).isEqualTo(tool.name);
        Assertions.assertThat(toolsListPage.getFirstUnitNameInTheList()).isEqualTo(toolUnit.unitName);
        Assertions.assertThat(toolsListPage.getFirstToolMFGInTheList()).isEqualTo(toolUnit.serialNumber);
        Assertions.assertThat(toolsListPage.getFirstToolUnitWarehouseLocationInTheList()).isEqualTo(toolUnit.location);
        Assertions.assertThat(toolsListPage.getFirstToolStatusLocationInTheList()).isEqualTo("Available");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        LocalDate actualDate = LocalDate.parse(toolsListPage.getFirstToolDateInTheList(), formatter);
        LocalDate today = LocalDate.now();
        Assertions.assertThat(actualDate)
                .as("Start date should be today's date")
                .isEqualTo(today);
    }


    ToolUnit editedToolUnit = new ToolUnit(
            "Impact Driver Unit A",  // unitName
            "SN-DRILL-12345",       // serialNumber
            "WarehouseMain",        // location
            250.00,                 // purchaseCost
            400.00                  // unitValue
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
        //addUnitsPage.selectFirstStatus();
        //addUnitsPage.setWarehouseUsingUtility(toolUnit.location);
        addUnitsPage.setPurchaseCost(toolUnit.purchaseCost);
        addUnitsPage.setValue(toolUnit.unitValue);

        addUnitsPage.clickSaveInformationButton();

        AlertUtils.waitForAlertVisible(page);
        page.waitForTimeout(1800);
        String alert = AlertUtils.getAlertText(page);
        Assertions.assertThat(alert).isEqualTo("Unit updated successfully");
        AlertUtils.waitForAlertHidden(page);


        Assertions.assertThat(toolsListPage.getFirstToolNameInTheList()).isEqualTo(toolName);
        Assertions.assertThat(toolsListPage.getFirstUnitNameInTheList()).isEqualTo(toolUnit.unitName);
        Assertions.assertThat(toolsListPage.getFirstToolMFGInTheList()).isEqualTo(toolUnit.serialNumber);
        Assertions.assertThat(toolsListPage.getFirstToolUnitWarehouseLocationInTheList()).isEqualTo(toolUnit.location);
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

        waitForElementRemoved(toolName);
        Assertions.assertThat(toolsListPage.getToolNamesList()).doesNotContain(toolName);

    }

}
