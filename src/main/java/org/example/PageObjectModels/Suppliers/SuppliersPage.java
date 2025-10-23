package org.example.PageObjectModels.Suppliers;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.util.List;

public class SuppliersPage {
    private final Page page;

    private final Locator addSupplierButton;
    private final Locator firstSupplierName;
    private final Locator supplierNames;
    private final Locator firstContactName;
    private final Locator firstSupplierEmail;
    private final Locator firstSupplierPhone;
    private final Locator firstSupplierCity;
    private final Locator threeDotsButton;
    private final Locator editSupplierButton;
    private final Locator deleteSupplierButton;
    private final Locator deleteSupplierInConfirmationModalButton;

    public SuppliersPage(Page page) {
        this.page = page;

        addSupplierButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Supplier"));
        firstSupplierName = page.locator("[class^='truncate'][href='/supplier']").first();
        supplierNames = page.locator("[class^='truncate'][href='/supplier']");
        firstContactName = page.locator("[role='cell'][data-field='contactName']").first();
        firstSupplierEmail = page.locator("[role='cell'][data-field='businessEmail']").first();
        firstSupplierPhone = page.locator("[role='cell'][data-field='phoneNumber']").first();
        firstSupplierCity = page.locator("[role='cell'][data-field='city']");
        threeDotsButton = page.getByTestId("MoreHorizIcon").first();
        editSupplierButton = page.getByRole(AriaRole.MENUITEM, new Page.GetByRoleOptions().setName("Edit"));
        deleteSupplierButton = page.getByRole(AriaRole.MENUITEM, new Page.GetByRoleOptions().setName("Delete"));
        deleteSupplierInConfirmationModalButton = page.getByRole(AriaRole.BUTTON, new  Page.GetByRoleOptions().setName("Delete"));

    }

    public SupplierPopUpPage clickAddSupplierButton() {
        addSupplierButton.click();
        return new SupplierPopUpPage(page);
    }

    public Locator addSupplierButtonLocator() {
        return addSupplierButton;
    }

    public String getFirstSupplierName() {
        return firstSupplierName.innerText();
    }

    public Locator getFirstSupplierNameLocator() {
        return firstSupplierName;
    }

    public String getFirstContactName() {
        return firstContactName.innerText();
    }

    public String getFirstSupplierEmail() {
        return firstSupplierEmail.innerText();
    }

    public String getFirstSupplierPhone() {
        return firstSupplierPhone.innerText();
    }

    public String getFirstSupplierCity() {
        return firstSupplierCity.innerText();
    }

    public void clickOnThreeDotsButton() {
        threeDotsButton.click();
    }

    public SupplierPopUpPage clickEditSupplierMenuButton() {
        editSupplierButton.click();
        return new SupplierPopUpPage(page);
    }

    public void clickDeleteSupplierMenuButton() {
        deleteSupplierButton.click();
    }

    public void clickDeleteSupplierInConfirmationModalButton() {
        deleteSupplierInConfirmationModalButton.click();
    }

    public List<String> getSuppliersList(){
        return supplierNames.allInnerTexts();
    }







}
