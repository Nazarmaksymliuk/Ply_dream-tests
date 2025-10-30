package org.example.PageObjectModels.Catalog;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.example.PageObjectModels.Kits.KitsCreationFlow.KitGeneralInformationPage;
import org.example.PageObjectModels.Material.MaterialsCreationFlow.MaterialSpecsPage;
import org.example.PageObjectModels.Tools.ToolsCreationFlow.AddEditUnitsPage;

import java.util.List;

public class CatalogPage {
    private final Page page;


    private final Locator addItemButton;
    private final Locator consumablesTab;
    private final Locator toolsTab;
    private final Locator kitsTab;
    private final Locator consumableitsTab;
    private final Locator firstRowMaterialThreeDots;
    private final Locator firstRowKitThreeDots;
    private final Locator menuMaterialEdit;
    private final Locator menuMaterialDelete;
    private final Locator menuItemDeleteTool;
    private final Locator editPricePenButton;
    private final Locator pageHeader;
    private final Locator materialNamesInTheList;
    private final Locator deleteMaterialInConfirmationModalButton;
    private final Locator deleteItemInConfirmationModalButton;
    private final Locator menuItemEditToolUnit;
    private final Locator menuKitInfoEdit;

    private final Locator firstRowToolThreeDots;


    private final Locator menuItemEdit;
    private final Locator menuItemDelete;




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
        kitsTab = page.getByRole(
                AriaRole.TAB,
                new Page.GetByRoleOptions().setName("Kits")
        );
        consumableitsTab = page.getByRole(
                AriaRole.TAB,
                new Page.GetByRoleOptions().setName("Consumables")
        );
        firstRowMaterialThreeDots = page
                .locator("[class^='_table_body_']")
                .locator("[data-testid='MoreHorizIcon']").first();

        firstRowToolThreeDots = page
                .locator("[class^='relative']")
                .locator("[data-testid='MoreHorizIcon']").first();

        firstRowKitThreeDots = page
                .locator("[data-testid='MoreHorizIcon']").first();

        menuMaterialEdit = page.getByRole(
                AriaRole.MENUITEM,
                new Page.GetByRoleOptions().setName("Edit Material")
        );
        menuItemEdit = page.getByRole(
                AriaRole.MENUITEM,
                new Page.GetByRoleOptions().setName("Edit") //// MAINNNNNNNNNN
        );
        menuKitInfoEdit = page.getByRole(
                AriaRole.MENUITEM,
                new Page.GetByRoleOptions().setName("Edit Kit Info") //// MAINNNNNNNNNN
        );
        menuItemEditToolUnit = page.getByRole(
                AriaRole.MENUITEM,
                new Page.GetByRoleOptions().setName("Edit Unit Info")
        );
        menuMaterialDelete = page.getByRole(
                AriaRole.MENUITEM,
                new Page.GetByRoleOptions().setName("Delete Material")
        );
        menuItemDelete = page.getByRole(
                AriaRole.MENUITEM,
                new Page.GetByRoleOptions().setName("Delete")   ////MAINNNNNNNN
        );
        menuItemDeleteTool = page.getByRole(
                AriaRole.MENUITEM,
                new Page.GetByRoleOptions().setName("Delete Tool")
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

        deleteItemInConfirmationModalButton =
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Delete"));



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

    public <T> T chooseMenuActionEdit(Class<T> returnPageClass) {
        menuItemEdit.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        menuItemEdit.click();
        try {
            return returnPageClass.getDeclaredConstructor(Page.class).newInstance(page);
        } catch (Exception e) {
            throw new RuntimeException("Не вдалося створити екземпляр сторінки: " + returnPageClass.getSimpleName(), e);
        }
    }

    public void chooseMenuActionDelete() {
        menuItemDelete.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        menuItemDelete.click();

    }

    public void openConsumablesTab() {
        consumablesTab.click();
    }

    public void openToolsTab() {
        toolsTab.click();
    }
    public void openKitsTab() {
        kitsTab.click();
    }
    public void openConsumableTab() {
        consumableitsTab.click();
    }

    public void openFirstRowMaterialThreeDots() {
        firstRowMaterialThreeDots.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        firstRowMaterialThreeDots.click();
    }
    public void openFirstRowToolThreeDots() {
        firstRowToolThreeDots.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        firstRowToolThreeDots.click();
    }
    public void openFirstRowKitThreeDots() {
        firstRowKitThreeDots.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        firstRowKitThreeDots.click();
    }

    public MaterialSpecsPage chooseMenuEditMaterial() {
        menuMaterialEdit.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        menuMaterialEdit.click();
        return new MaterialSpecsPage(page);
    }

    public AddEditUnitsPage chooseMenuEditToolUnit() {
        menuItemEditToolUnit.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        menuItemEditToolUnit.click();
        return new AddEditUnitsPage(page);
    }
    public KitGeneralInformationPage chooseMenuEditKit() {
        menuKitInfoEdit.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        menuKitInfoEdit.click();
        return new KitGeneralInformationPage(page);
    }

    public MaterialSpecsPage chooseMenuDeleteMaterial() {
        menuMaterialDelete.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        menuMaterialDelete.click();
        return new MaterialSpecsPage(page);
    }
    public MaterialSpecsPage chooseMenuDeleteTool() {
        menuItemDeleteTool.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        menuItemDeleteTool.click();
        return new MaterialSpecsPage(page);
    }

    public void clickEditPricePen() {
        editPricePenButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        editPricePenButton.click();
    }
    public void confirmDeleteMaterialInModal() {
        deleteMaterialInConfirmationModalButton.click();
    }

    public void confirmDeleteItemInModal() {
        deleteItemInConfirmationModalButton.click();
    }

    public List<String> getMaterialNamesList(){
        return materialNamesInTheList.allInnerTexts();
    }



}
