package org.example.UI.PageObjectModels.Material;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.example.UI.PageObjectModels.Material.MaterialsCreationFlow.MaterialSpecsPage;

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
    private final Locator firstRowMaterialStockThreeDots;

    private final Locator menuItemEditAvailability;

    private final Locator qtySpan;



    public MaterialsListPage(Page page) {
        this.page = page;

        materialFirstNameInTheList = page.locator("a.link_black[href^='/material/']").first();
        materialNamesInTheList      = page.locator("a.link_black[href^='/material/']");
        firstItemNumberInTheList    = page.locator(".cursor-pointer.w-fit").first();
        firstMaterialVariation      = page.locator(".status_xs.variation_name").first();

        firstRowThreeDots = page.locator("[class^='_table_item_'][data-testid='MoreHorizIcon']");
        menuItemEdit      = page.getByRole(AriaRole.MENUITEM,   new Page.GetByRoleOptions().setName("Edit Material"));
        menuItemEditAvailability      = page.getByRole(AriaRole.MENUITEM,   new Page.GetByRoleOptions().setName("Edit availability"));
        menuItemDelete    = page.getByRole(AriaRole.MENUITEM,   new Page.GetByRoleOptions().setName("Delete"));
        deleteMaterialInConfirmationModalButton =
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Delete"));

        openLocationsListButton     = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Locations"));
        firstLocationArrowDownButton= page.getByTestId("KeyboardArrowDownIcon").first();
        materialLocationInTheDropDown = page.locator("[href^='/stock/warehouse']");
        qtyInMaterialLocation       = page.locator("b.flex.items-center.gap-1");

        firstRowMaterialStockThreeDots = page.locator("[class^='_table_item_'] [data-testid='MoreHorizIcon']").first();

        qtySpan = page.locator("div[role='button'][class^='_edit_wrapper_'] b span:nth-of-type(1)").first();

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
    public MaterialSpecsPage chooseMenuEditMaterialAvailability() {
        menuItemEditAvailability.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        menuItemEditAvailability.click();
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

    public double getQtyFromMaterialLocation() {
        String text = qtyInMaterialLocation.innerText().replaceAll("[^\\d.]+", ""); // залишає цифри й крапку
        if (text.isEmpty()) return 0.0;
        return Double.parseDouble(text);
    }
    public void openFirstRowMaterialStockThreeDots() {
        firstRowMaterialStockThreeDots.waitFor(
                new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE)
        );
        firstRowMaterialStockThreeDots.click();
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

    /** Кількість з першого рядка гріда (значення у <b><span>10</span>...) */
    public int getFirstRowQuantity() {
        Locator qtySpan = page.locator("div[role='button'][class^='_edit_wrapper_'] b span:nth-of-type(1)").first();
        String text = qtySpan.innerText().trim();

        // приберемо все, крім цифр
        text = text.replaceAll("[^\\d]", "");

        // якщо раптом пусто — повернемо 0, щоб уникнути NumberFormatException
        if (text.isEmpty()) return 0;

        return Integer.parseInt(text);
    }


}
