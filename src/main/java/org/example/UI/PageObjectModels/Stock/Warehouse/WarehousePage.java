package org.example.UI.PageObjectModels.Stock.Warehouse;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.example.UI.PageObjectModels.Material.MaterialsCreationFlow.MaterialSpecsPage;
import org.example.UI.PageObjectModels.Stock.InventoryCount.InventoryCountListPage;

public class WarehousePage {private final Page page;

    // === Locators ===
    private final Locator SetupMaterialsButton;
    private final Locator consumablesTabButton;

    private final Locator toolsTabButton;
    private final Locator kitsTabButton;

    private final Locator inventoryCountTabButton;


    // === Constructor ===
    public WarehousePage(Page page) {
        this.page = page;

        // Playwright-friendly selectors замість XPath
        SetupMaterialsButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Setup Materials")
        );

        consumablesTabButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Consumables")
        );
        toolsTabButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Tools")
        );
        kitsTabButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Kits")
        );

        inventoryCountTabButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Inventory Counts"));



    }

    public MaterialSpecsPage clickOnAddNewMaterialButton() {
        SetupMaterialsButton.waitFor(
                new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE)
        );
        SetupMaterialsButton.click();
        return new MaterialSpecsPage(page);
    }

    public void waitForLoaded() {
        SetupMaterialsButton.waitFor(
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
    public void clickOnKitsTabButton() {
        kitsTabButton.waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                .setTimeout(10_000)); // чекаємо до 10 секунд, поки стане видимим
        kitsTabButton.click();
    }
    public void clickOnConsumablesTabButton() {
        consumablesTabButton.waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                .setTimeout(10_000)); // чекаємо до 10 секунд, поки стане видимим
        consumablesTabButton.click();
    }

    public InventoryCountListPage clickOnInventoryCountTabButton() {
        inventoryCountTabButton.waitFor(
                new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE)
        );
        inventoryCountTabButton.click();
        return new InventoryCountListPage(page);
    }

}
