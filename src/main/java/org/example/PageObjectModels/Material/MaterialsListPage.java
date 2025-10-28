package org.example.PageObjectModels.Material;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.example.PageObjectModels.Material.MaterialsCreationFlow.MaterialSpecsPage;

import java.util.List;

public class MaterialsListPage {
    private final Page page;

    private final Locator materialFirstNameInTheList;
    private final Locator materialNamesInTheList;
    private final Locator firstItemNumberInTheList;
    private final Locator firstMaterialVariation;

    private final Locator firstRowThreeDots;
    private final Locator menuItemEdit;
    private final Locator menuItemDelete;
    private final Locator deleteMaterialInConfirmationModalButton;

    private final Locator openLocationsListButton;
    private final Locator firstLocationArrowDownButton;
    private final Locator materialLocationInTheDropDown;
    private final Locator qtyInMaterialLocation;

    public MaterialsListPage(Page page) {
        this.page = page;

        materialFirstNameInTheList = page.locator("a.link_black[href^='/material/']").first();
        materialNamesInTheList      = page.locator("a.link_black[href^='/material/']");
        firstItemNumberInTheList    = page.locator(".cursor-pointer.w-fit").first();
        firstMaterialVariation      = page.locator(".status_xs.variation_name").first();

        firstRowThreeDots = page.locator("[div^='_table_item_'][data-testid='MoreHorizIcon']");
        menuItemEdit      = page.getByRole(AriaRole.MENUITEM,   new Page.GetByRoleOptions().setName("Edit Material"));
        menuItemDelete    = page.getByRole(AriaRole.MENUITEM,   new Page.GetByRoleOptions().setName("Delete Material"));
        deleteMaterialInConfirmationModalButton =
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Delete Material"));

        openLocationsListButton     = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Locations"));
        firstLocationArrowDownButton= page.getByTestId("KeyboardArrowDownIcon").first();
        materialLocationInTheDropDown = page.locator("[href^='/stock/warehouse']");
        qtyInMaterialLocation       = page.locator("b.flex.items-center.gap-1");
    }

    // ===== Waits =====
    public void waitFirstRowVisible() {
        materialFirstNameInTheList.waitFor(
                new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(60000)
        );
    }

    // ===== Row menus =====
    public void openFirstRowThreeDots() {
        firstRowThreeDots.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        firstRowThreeDots.click();
    }

    public MaterialSpecsPage chooseMenuEditMaterial() {
        menuItemEdit.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        menuItemEdit.click();
        return new MaterialSpecsPage(page);
    }

    public void chooseMenuDeleteMaterial() {
        menuItemDelete.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        menuItemDelete.click();
    }

    public void confirmDeleteMaterialInModal() {
        deleteMaterialInConfirmationModalButton.click();
    }

    // ===== Locations dropdown / qty =====
    public void openLocationsList() {
        openLocationsListButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        openLocationsListButton.click();
    }

    public void clickFirstLocationArrowDown() {
        firstLocationArrowDownButton.click();
    }

    public String getMaterialLocationFromDropdown() {
        return materialLocationInTheDropDown.innerText();
    }

    public String getQtyFromMaterialLocation() {
        return qtyInMaterialLocation.innerText().replaceAll("\\D+", "");
    }

    // ===== Getters from list =====
    public String getFirstMaterialNameInTheList() {
        return materialFirstNameInTheList.innerText();
    }

    public String getFirstItemNumberInTheList() {
        return firstItemNumberInTheList.innerText();
    }

    public String getFirstMaterialVariation() {
        return firstMaterialVariation.innerText();
    }

    public List<String> getMaterialNamesList() {
        return materialNamesInTheList.allInnerTexts();
    }
}
