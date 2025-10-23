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
    private final Locator toolsTab;
    private final Locator firstRowThreeDots;
    private final Locator menuItemEdit;
    private final Locator menuItemDelete;
    private final Locator editPricePenButton;
    private final Locator pageHeader;
    private final Locator materialNamesInTheList;
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
        toolsTab = page.getByRole(
                AriaRole.TAB,
                new Page.GetByRoleOptions().setName("Tools")
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
        editPricePenButton = page
                .getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Edit price"))
                .first();

        pageHeader = page
                .getByText("Catalog")
                .or(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Catalog")));
        materialNamesInTheList      = page.locator("a.link_black[href^='/material/']");
        deleteMaterialInConfirmationModalButton =
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Delete Material"));


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

    public void openToolsTab() {
        toolsTab.click();
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

    public void clickEditPricePen() {
        editPricePenButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        editPricePenButton.click();
    }
    public void confirmDeleteMaterialInModal() {
        deleteMaterialInConfirmationModalButton.click();
    }

    public List<String> getMaterialNamesList(){
        return materialNamesInTheList.allInnerTexts();
    }



}
