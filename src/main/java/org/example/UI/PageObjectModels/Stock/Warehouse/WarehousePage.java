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

    private final Locator toolsTabButton;

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
        toolsTabButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Tools")
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

    public void clickOnToolsTabButton() {
        toolsTabButton.waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                .setTimeout(10_000)); // чекаємо до 10 секунд, поки стане видимим
        toolsTabButton.click();        toolsTabButton.click();
    }

}
