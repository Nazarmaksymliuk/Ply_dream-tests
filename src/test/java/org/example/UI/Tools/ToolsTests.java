package org.example.UI.Tools;

import org.assertj.core.api.Assertions;
import org.example.BaseTestExtension.PlaywrightBaseTest;
import org.example.Models.Tool;
import org.example.Models.ToolUnit;
import org.example.PageObjectModels.Alerts.AlertUtils;
import org.example.PageObjectModels.Catalog.CatalogPage;
import org.example.PageObjectModels.Catalog.ToolsTab.AddUnitsPage;
import org.example.PageObjectModels.Catalog.ToolsTab.GeneralInformationPage;
import org.example.PageObjectModels.Tools.ToolsListPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class ToolsTests extends PlaywrightBaseTest {
    CatalogPage catalogPage;
    ToolsListPage toolsListPage;
    GeneralInformationPage generalInformationPage;
    AddUnitsPage addUnitsPage;

    @BeforeEach
    public void setUp() {
        openPath("/catalog");
        catalogPage = new CatalogPage(page);
        toolsListPage = new ToolsListPage(page);
    }

    Tool tool = new Tool(
            "Tool-" + new Random().nextInt(100000),     // name
            "MFG-" + new Random().nextInt(100000),                 // mfgNumber
            "High-performance tool for any type of work.",
            "test tag"// description
    );

    ToolUnit toolUnit = new ToolUnit(
            "Impact Driver Unit A",  // unitName
            "SN-DRILL-12345",       // serialNumber
            "WarehouseMain",        // location
            250.00,                 // purchaseCost
            400.00                  // unitValue
    );

    @DisplayName("Create Tool in the Catalog Test")
    @Test
    public void testCreateTool() {

        catalogPage.waitForLoaded();

        catalogPage.openToolsTab();

        generalInformationPage = catalogPage.clickAddItem(GeneralInformationPage.class);
        generalInformationPage.setToolName(tool.name);
        generalInformationPage.setMfgNumber(tool.mfgNumber);
        generalInformationPage.setToolDescription(tool.description);
        generalInformationPage.setTags(tool.tags);

        addUnitsPage = generalInformationPage.clickNextPage();

        addUnitsPage.setUnitName(toolUnit.unitName);
        addUnitsPage.setSerialNumber(toolUnit.serialNumber);
        addUnitsPage.selectFirstStatus();
        addUnitsPage.setWarehouseUsingUtility(toolUnit.location);
        addUnitsPage.setPurchaseCost(toolUnit.purchaseCost);
        addUnitsPage.setValue(toolUnit.unitValue);

        addUnitsPage.clickSaveInformationButton();

        addUnitsPage.clickSaveButton();

        AlertUtils.waitForAlertVisible(page);
        String alert = AlertUtils.getAlertText(page);
        Assertions.assertThat(alert).isEqualTo("Tool"  + tool.name + "has been successfully created");
        AlertUtils.waitForAlertHidden(page);

        Assertions.assertThat(toolsListPage.getFirstToolNameInTheList()).isEqualTo(tool.name);
        Assertions.assertThat(toolsListPage.getFirstUnitNameInTheList()).isEqualTo(toolUnit.unitName);
        Assertions.assertThat(toolsListPage.getFirstToolMFGInTheList()).isEqualTo(tool.mfgNumber);
        Assertions.assertThat(toolsListPage.getFirstToolUnitWarehouseLocationInTheList()).isEqualTo(toolUnit.location);
        Assertions.assertThat(toolsListPage.getFirstToolStatusLocationInTheList()).isEqualTo("Available");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        LocalDate actualDate = LocalDate.parse(toolsListPage.getFirstToolDateInTheList(), formatter);
        LocalDate today = LocalDate.now();
        Assertions.assertThat(actualDate)
                .as("Start date should be today's date")
                .isEqualTo(today);


    }
}
