package org.example.PageObjectModels.Catalog;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.example.PageObjectModels.Catalog.MaterialsTab.MaterialSpecsPage;

import java.util.List;

public class CatalogPage {
    private final Page page;


    private final Locator addItemButton;
    private final Locator consumablesTab;
    private final Locator firstRowThreeDots;
    private final Locator menuItemEdit;
    private final Locator menuItemDelete;
    private final Locator editPricePenButton;
    private final Locator openLocationsListButton;
    private final Locator pageHeader;
    private final Locator materialFirstNamesInTheList;
    private final Locator materialNamesInTheList;
    private final Locator firstItemNumberInTheList;
    private final Locator firstMaterialVariation;
    private final Locator firstLocationArrowDownButton;
    private final Locator materialLocationInTheDropDown;
    private final Locator qtyInMaterialLocation;
    private final Locator deleteMaterialInConfirmationModalButton;


    public CatalogPage(Page page) {
        this.page = page;

        addItemButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Add Item")
        );
        consumablesTab = page.getByRole(
                AriaRole.TAB,
                new Page.GetByRoleOptions().setName("Consumables")
        );
        firstRowThreeDots = page
                .locator("[div^='_table_item_'][data-testid='MoreHorizIcon']");

        menuItemEdit = page.getByRole(
                AriaRole.MENUITEM,
                new Page.GetByRoleOptions().setName("Edit Material")
        );
        menuItemDelete = page.getByRole(
                AriaRole.MENUITEM,
                new Page.GetByRoleOptions().setName("Delete Material")
        );
        deleteMaterialInConfirmationModalButton = page.getByRole(AriaRole.BUTTON, new  Page.GetByRoleOptions().setName("Delete Material"));
        editPricePenButton = page
                .getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Edit price"))
                .first();
        openLocationsListButton = page
                .getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Locations"))
                .or(page.locator("(//button[@type='button'])[19]")); // fallback, по можливості прибрати
        pageHeader = page
                .getByText("Catalog")
                .or(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Catalog")));
        materialFirstNamesInTheList = page.locator("a.link_black[href^='/material/']").first();
        materialNamesInTheList = page.locator("a.link_black[href^='/material/']");
        firstItemNumberInTheList = page.locator(".cursor-pointer.w-fit").first();
        firstMaterialVariation = page.locator(".status_xs.variation_name").first();
        firstLocationArrowDownButton = page.getByTestId("KeyboardArrowDownIcon").first();
        materialLocationInTheDropDown = page.locator("[href='/stock/warehouse']");
        qtyInMaterialLocation = page.locator("b.flex.items-center.gap-1");

    }

    public void waitForLoaded() {
        pageHeader.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE).setTimeout(60000));
    }

    public <T> T clickAddItem(Class<T> pageClass) {
        addItemButton.waitFor(
                new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE)
        );
        addItemButton.click();
        try {
            // шукає конструктор, який приймає Page
            return pageClass.getDeclaredConstructor(Page.class).newInstance(page);
        } catch (Exception e) {
            throw new RuntimeException("Cannot create page: " + pageClass.getName(), e);
        }
    }

    public void openConsumablesTab() {
        consumablesTab.click();
    }

    public void openFirstRowThreeDots() {
        firstRowThreeDots.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        firstRowThreeDots.click();
    }

    public MaterialSpecsPage chooseMenuEditMaterial() {
        menuItemEdit.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        menuItemEdit.click();
        return new MaterialSpecsPage(page);
    }
    public MaterialSpecsPage chooseMenuDeleteMaterial() {
        menuItemDelete.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        menuItemDelete.click();
        return new MaterialSpecsPage(page);
    }

    public void clickDeleteMaterialInConfirmationModalButton(){
        deleteMaterialInConfirmationModalButton.click();
    }

    public void clickEditPricePen() {
        editPricePenButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        editPricePenButton.click();
    }

    public void openLocationsList() {
        openLocationsListButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        openLocationsListButton.click();
    }

    public String getFirstMaterialNameInTheList(){
        return materialFirstNamesInTheList.innerText();
    }

    public String getFirstItemNumberInTheList(){
        return firstItemNumberInTheList.innerText();
    }

    public String getMaterialVariation(){
        return firstMaterialVariation.innerText();
    }

    public void clickOnTheFirstLocationArrowDownButton(){
        firstLocationArrowDownButton.click();
    }

    public String getMaterialLocationInTheDropDown(){
        return materialLocationInTheDropDown.innerText();
    }

    public String getQtyInMaterialLocation(){
        return qtyInMaterialLocation.innerText().replaceAll("\\D+", "");
    }

    public List<String> getMaterialNamesList(){
        return materialNamesInTheList.allInnerTexts();
    }



}
