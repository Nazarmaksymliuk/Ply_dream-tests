package org.example.UI.PageObjectModels.Stock.Warehouse;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.example.UI.PageObjectModels.Consumable.ConsumableCreationFlow.ConsumableGeneralInfoPage;
import org.example.UI.PageObjectModels.Kits.KitsCreationFlow.KitGeneralInformationPage;
import org.example.UI.PageObjectModels.Material.MaterialsCreationFlow.MaterialSpecsPage;
import org.example.UI.PageObjectModels.Stock.InventoryCount.InventoryCountListPage;

public class WarehousePage {
    private final Page page;

    // === Locators ===
    private final Locator SetupMaterialsButton;
    private final Locator consumablesTabButton;
    private final Locator addNewKitButton;

    private final Locator toolsTabButton;
    private final Locator kitsTabButton;

    private final Locator inventoryCountTabButton;
    private final Locator firstThreeDotsInTheList;
    private final Locator editKitButton;
    private final Locator editConsumableButton;
    private final Locator deleteButton;
    private final Locator deleteItemInConfirmationModalButton;
    private final Locator addConsumableButton;

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

        addNewKitButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Add kit")
        );

        inventoryCountTabButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Inventory Counts"));

        this.firstThreeDotsInTheList = page.getByTestId("ply_kit_item_line_actions_button");

        this.editKitButton = page.getByRole(AriaRole.MENUITEM, new Page.GetByRoleOptions().setName("Edit Kit Info"));

        this.editConsumableButton = page.getByRole(AriaRole.MENUITEM, new Page.GetByRoleOptions().setName("Edit consumable"));

        this.deleteButton = page.getByRole(AriaRole.MENUITEM, new Page.GetByRoleOptions().setName("Delete"));

        deleteItemInConfirmationModalButton =
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Delete"));

        addConsumableButton =
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Consumable"));

    }

    public MaterialSpecsPage clickOnAddNewMaterialButton() {
        SetupMaterialsButton.waitFor(
                new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE)
        );
        SetupMaterialsButton.click();
        return new MaterialSpecsPage(page);
    }

    public KitGeneralInformationPage clickOnAddNewKitButton() {
        addNewKitButton.waitFor(
                new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE)
        );
        addNewKitButton.click();
        return new KitGeneralInformationPage(page);
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

    public ConsumableGeneralInfoPage clickAddConsumable(){
        addConsumableButton.click();
        return new ConsumableGeneralInfoPage(page);
    }

    public void openFirstRowThreeDots(){
        firstThreeDotsInTheList.click();
    }

    public KitGeneralInformationPage clickOnEditKitButton(){
        editKitButton.click();
        return new KitGeneralInformationPage(page);
    }

    public ConsumableGeneralInfoPage clickOnEditConsumableButton(){
        editConsumableButton.click();
        return new ConsumableGeneralInfoPage(page);
    }

    public void clickOnDeleteButton(){
        deleteButton.click();
    }

    public void confirmDeletion(){
        deleteItemInConfirmationModalButton.click();
    }

    public InventoryCountListPage clickOnInventoryCountTabButton() {
        inventoryCountTabButton.waitFor(
                new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE)
        );
        inventoryCountTabButton.click();
        return new InventoryCountListPage(page);
    }

}
