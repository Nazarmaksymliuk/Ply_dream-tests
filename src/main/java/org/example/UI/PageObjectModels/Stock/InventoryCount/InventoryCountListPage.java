package org.example.UI.PageObjectModels.Stock.InventoryCount;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

// ===== List (tab) =====
public class InventoryCountListPage {
    private final Page page;

    // Кнопка "Create Inventory Count"
    private final Locator createInventoryCountButton;

    private final Locator firstCycleCountTodayDateLocator;

    private final Locator firstThreeDotsButton;
    private final Locator cycleCountLocator;
    private final Locator menuItemDelete;
    private final Locator menuItemEdit;
    private final Locator deleteButtonInConfirmationModal;

    public InventoryCountListPage(Page page) {
        this.page = page;
        this.createInventoryCountButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Create Inventory Count")
        );

        firstCycleCountTodayDateLocator = page.locator("[class^='_creation_date_']").first();

        cycleCountLocator = page.locator("[href^='/stock']").nth(1);

        firstThreeDotsButton = cycleCountLocator.locator("[data-testid='MoreHorizIcon']") // fallback: знайти контейнер
                .first();

        menuItemDelete = page.getByRole(
                AriaRole.MENUITEM,
                new Page.GetByRoleOptions().setName("Delete")
        );
        menuItemEdit = page.getByRole(
                AriaRole.MENUITEM,
                new Page.GetByRoleOptions().setName("Edit")
        );

        deleteButtonInConfirmationModal = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Delete"));
    }

    /** Відкрити форму створення інвентаризації. */
    public CreateInventoryCountPage clickCreateInventoryCountButton() {
        createInventoryCountButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        createInventoryCountButton.click();
        return new CreateInventoryCountPage(page);
    }

    public Locator getFirstCycleCountTodayDate(){
        return firstCycleCountTodayDateLocator;
    }

    public String getFirstCycleCountTodayDateString(){
        return firstCycleCountTodayDateLocator.innerText();
    }

    public void clickFirstThreeDotsButton() {
        firstThreeDotsButton.click();
    }

    public void clickOnDeleteButton(){
        menuItemDelete.click();
    }
    public void clickOnEditButton(){
        menuItemEdit.click();
    }
    public void clickOnDeleteButtonInConfirmationModal(){
        deleteButtonInConfirmationModal.click();
    }



}
