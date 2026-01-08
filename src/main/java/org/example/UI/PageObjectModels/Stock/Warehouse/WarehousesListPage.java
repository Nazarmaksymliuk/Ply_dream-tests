package org.example.UI.PageObjectModels.Stock.Warehouse;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.util.List;

public class WarehousesListPage {
    private final Page page;

    // === Locators ===
    private final Locator addWarehouseButton;
    private final Locator firstRowThreeDots;      // три крапки у першому складі
    private final Locator menuItemEdit;           // пункт меню Edit
    private final Locator menuItemDelete;
    private final Locator deleteConfirmationInputField;
    private final Locator deleteWarehouseInConfirmationModalButton;
    private final Locator firstWarehouseLink;     // перший склад у списку
    private final Locator mainWarehouseLink;      // конкретно WarehouseMain (за href)
    private final Locator warehousesNamesList;
    private final Locator warehousesAddressList;
    private final Locator firstWarehouseName;


    public WarehousesListPage(Page page) {
        this.page = page;

        addWarehouseButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Add Warehouse")
        );

        firstWarehouseLink = page.locator("a[href*='/stock/warehouse/']").first();

        firstWarehouseName = page.locator("[class^='_name_']").first();

        warehousesNamesList = page.locator("[class^='_name_']");

        warehousesAddressList = page.locator("[class^='_address_']");

        mainWarehouseLink = page.locator("a[href*='/stock/warehouse/warehousemain']");

        firstRowThreeDots = firstWarehouseLink
                .locator("[data-testid='MoreHorizIcon']") // fallback: знайти контейнер
                .first();

        menuItemEdit = page.getByRole(
                AriaRole.MENUITEM,
                new Page.GetByRoleOptions().setName("Edit")
        );

        menuItemDelete = page.getByRole(
                AriaRole.MENUITEM,
                new Page.GetByRoleOptions().setName("Delete")
        );

        deleteConfirmationInputField = page.locator("div.m_20 input[type='text']");


        deleteWarehouseInConfirmationModalButton = page.getByRole(
                AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Delete without transferring")
        );



    }

    public CreateWarehousePopUpPage clickOnAddWarehouseButton() {
        addWarehouseButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        addWarehouseButton.click();
        return new CreateWarehousePopUpPage(page);
    }

    public void clickOnWarehouseThreeDotsButton() {
        // переконаймося, що перший склад є видимим (щоб не ловити гонки)
        firstRowThreeDots.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        firstRowThreeDots.click();
    }

    public EditWarehousePopUpPage clickOnEditWarehouseButton() {
        menuItemEdit.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        menuItemEdit.click();
        return new EditWarehousePopUpPage(page);
    }

    public void clickOnDeleteButton(){
        menuItemDelete.click();
    }

    public void fillDeleteInTheConfirmationInputField() {
        deleteConfirmationInputField.click();
        deleteConfirmationInputField.fill("delete");
    }

    public void clickDeleteWarehouseInConfirmationModalButton() {
        deleteWarehouseInConfirmationModalButton.click();
    }

    public WarehousePage clickOnFirstWarehouse() {
        firstWarehouseLink.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        firstWarehouseLink.click();
        return new WarehousePage(page);
    }

    public WarehousePage clickOnMainWarehouse() {
        mainWarehouseLink.scrollIntoViewIfNeeded();
        mainWarehouseLink.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        mainWarehouseLink.click();
        return new WarehousePage(page);
    }

    public String getFirstWarehouseName(){
        return firstWarehouseName.first().innerText();
    }

    public List<String> getWarehousesNamesList() {
        return warehousesNamesList.allInnerTexts();
    }
    public List<String> getWarehousesAddressList(){
        return warehousesAddressList.allInnerTexts();
    }

    // (опційно) чек готовності списку складів
    public void waitForLoaded() {
        firstWarehouseLink.waitFor(
                new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(15000)
        );
    }
}
