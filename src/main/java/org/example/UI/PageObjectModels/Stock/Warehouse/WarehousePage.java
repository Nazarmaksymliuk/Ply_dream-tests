package org.example.UI.PageObjectModels.Stock.Warehouse;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.example.UI.PageObjectModels.Material.MaterialsCreationFlow.MaterialSpecsPage;

public class WarehousePage {private final Page page;

    // === Locators ===
    private final Locator addNewMaterialButton;
    private final Locator consumablesTabButton;

    // === Constructor ===
    public WarehousePage(Page page) {
        this.page = page;

        // Playwright-friendly selectors замість XPath
        addNewMaterialButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Add material")
        );

        consumablesTabButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Consumables")
        );
    }

    public MaterialSpecsPage clickOnAddNewMaterialButton() {
        addNewMaterialButton.waitFor(
                new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE)
        );
        addNewMaterialButton.click();
        return new MaterialSpecsPage(page);
    }

    public void waitForLoaded() {
        addNewMaterialButton.waitFor(
                new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(15000)
        );
    }
}
