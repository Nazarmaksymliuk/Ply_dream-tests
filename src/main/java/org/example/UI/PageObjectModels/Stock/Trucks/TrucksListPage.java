package org.example.UI.PageObjectModels.Stock.Trucks;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.util.List;

import static org.example.UI.PageObjectModels.Utils.Waits.WaitUtils.waitForVisible;

public class TrucksListPage {
    private final Page page;

    private final Locator addTruckButton;
    private final Locator firstTruckThreeDots;

    private final Locator menuItemEdit;
    private final Locator menuItemDelete;
    private final Locator confirmDeleteButton;
    private final Locator trucksListRoot;
    private final Locator firstTruckLink;

    public TrucksListPage(Page page) {
        this.page = page;

        addTruckButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Truck"));
        trucksListRoot = page.locator("#root");

        firstTruckLink = page.locator("a[href*='/stock/truck/']").first();

        // Якщо у вас три крапки як у складів — цей селектор підійде
        firstTruckThreeDots = firstTruckLink.locator("[data-testid='MoreHorizIcon']").first();

        menuItemEdit = page.getByRole(AriaRole.MENUITEM, new Page.GetByRoleOptions().setName("Edit").setExact(true));
        menuItemDelete = page.getByRole(AriaRole.MENUITEM, new Page.GetByRoleOptions().setName("Delete"));
        confirmDeleteButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Delete"));


    }

    public void waitForLoaded() {
        addTruckButton.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(15000));
    }

    public TruckFormPopUpPage clickOnAddTruckButton() {
        addTruckButton.click();
        return new TruckFormPopUpPage(page);
    }

    public void clickOnTruckThreeDotsButton() {
        firstTruckThreeDots.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        firstTruckThreeDots.click();
    }

    public TruckFormPopUpPage clickOnEditTruckButton() {
        menuItemEdit.click();
        return new TruckFormPopUpPage(page);
    }

    public void clickOnDeleteButton() { menuItemDelete.click(); }

    public void confirmDeleteInModal() { confirmDeleteButton.click(); }

    public Locator root() { return trucksListRoot; }

    public List<String> getTrucksNamesList() {
        waitForVisible(firstTruckLink);
        return page.locator("[class*='_name_']").allInnerTexts(); // заміни на правильний клас
    }
}
