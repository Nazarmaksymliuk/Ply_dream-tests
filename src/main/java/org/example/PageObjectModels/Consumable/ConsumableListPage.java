package org.example.PageObjectModels.Consumable;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.example.PageObjectModels.Consumable.ConsumableCreationFlow.ConsumableGeneralInfoPage;

import java.util.List;

public class ConsumableListPage {
    private final Page page;

    // === Locators ===
    private final Locator consumableFirstNameInTheList;
    private final Locator consumableNamesInTheList;
    private final Locator firstItemNumberInTheList;
    private final Locator firstConsumableVariation;

    private final Locator firstRowThreeDots;
    private final Locator menuItemEdit;
    private final Locator menuItemDelete;
    private final Locator deleteConsumableInConfirmationModalButton;

    private final Locator openLocationsListButton ;
    private final Locator firstLocationArrowDownButton;
    private final Locator consumableLocationInTheDropDown;
    private final Locator qtyInConsumableLocation;
    private final Locator consumablePriceDiv;



    // === Constructor ===
    public ConsumableListPage(Page page) {
        this.page = page;

        // таблиця списку consumables
        consumableFirstNameInTheList = page.locator("[href^='/consumable/']").first();
        consumableNamesInTheList     = page.locator("[href^='/consumable/']");
        firstItemNumberInTheList     = page.locator(".cursor-pointer.w-fit").first();
        firstConsumableVariation     = page.locator(".status_xs.variation_name").first();

        // три крапки меню
        firstRowThreeDots = page.locator("[div^='_table_item_'][data-testid='MoreHorizIcon']");
        menuItemEdit      = page.getByRole(AriaRole.MENUITEM, new Page.GetByRoleOptions().setName("Edit Consumable"));
        menuItemDelete    = page.getByRole(AriaRole.MENUITEM, new Page.GetByRoleOptions().setName("Delete Consumable"));
        deleteConsumableInConfirmationModalButton =
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Delete Consumable"));

        // Locations dropdown / quantity
        openLocationsListButton       = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Locations"));
        firstLocationArrowDownButton  = page.getByTestId("KeyboardArrowDownIcon").first();
        consumableLocationInTheDropDown = page.locator("[href^='/stock/warehouse']");
        qtyInConsumableLocation       = page.locator("b.flex.items-center.gap-1");

        consumablePriceDiv = page.locator("[class^='_table_body_']").locator("[class*='font-semibold']").nth(3);

    }

    // ===== Waits =====
    public void waitFirstRowVisible() {
        consumableFirstNameInTheList.waitFor(
                new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(60000)
        );
    }

    // ===== Row menus =====
    public void openFirstRowThreeDots() {
        firstRowThreeDots.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        firstRowThreeDots.click();
    }

    public ConsumableGeneralInfoPage chooseMenuEditConsumable() {
        menuItemEdit.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        menuItemEdit.click();
        return new ConsumableGeneralInfoPage(page);
    }

    public void chooseMenuDeleteConsumable() {
        menuItemDelete.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        menuItemDelete.click();
    }

    public void confirmDeleteConsumableInModal() {
        deleteConsumableInConfirmationModalButton.click();
    }

    // ===== Locations dropdown / qty =====
    public void openLocationsList() {
        openLocationsListButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        openLocationsListButton.click();
    }

    public void clickFirstLocationArrowDown() {
        firstLocationArrowDownButton.click();
    }

    public String getConsumableLocationFromDropdown() {
        return consumableLocationInTheDropDown.innerText();
    }

    public double getQtyFromConsumableLocation() {
        String text = qtyInConsumableLocation.innerText().replaceAll("[^\\d.]+", ""); // залишає лише цифри та крапку
        if (text.isEmpty()) return 0.0;
        return Double.parseDouble(text);
    }

    // ===== Getters from list =====
    public String getFirstConsumableNameInTheList() {
        return consumableFirstNameInTheList.innerText();
    }


    public String getFirstItemNumberInTheList() {
        return firstItemNumberInTheList.innerText();
    }

    public String getFirstConsumableVariation() {
        return firstConsumableVariation.innerText();
    }

    public List<String> getConsumableNamesList() {
        return consumableNamesInTheList.allInnerTexts();
    }

    public Double getTheConsumablePrice() {

        consumablePriceDiv.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        String priceText = consumablePriceDiv.innerText();

        priceText = priceText.replaceAll("[^0-9.,]", ""); // залишає лише цифри і роздільник
        priceText = priceText.replace(",", "."); // якщо десь є кома замість крапки

        return Double.parseDouble(priceText);
    }
}
