package org.example.PageObjectModels.Suppliers;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.time.LocalDate;

public class SupplierPopUpPage {
    private final Page page;

    private final Locator supplierName;
    private final Locator supplierCity;
    private final Locator supplierNote;
    private final Locator supplierContactName;
    private final Locator supplierContactEmail;
    private final Locator supplierContactPhone;
    private final Locator saveButton;

    public SupplierPopUpPage(Page page) {
        this.page = page;

        supplierName = page.getByPlaceholder("Enter supplier name");
        supplierCity = page.getByPlaceholder("Enter city");
        supplierNote = page.getByPlaceholder("Enter note");
        supplierContactName = page.getByPlaceholder("Enter contact name");
        supplierContactEmail = page.getByPlaceholder("Enter email");
        supplierContactPhone = page.getByPlaceholder("Enter phone number");
        saveButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save"));
    }

    public void setSupplierName(String supplierNameValue) {
        supplierName.fill(supplierNameValue);
    }
    public void setSupplierCity(String supplierCityValue) {
        supplierCity.fill(supplierCityValue);
    }
    public void setSupplierNote(String supplierNoteValue) {
        supplierNote.fill(supplierNoteValue);
    }
    public void setSupplierContactName(String supplierContactNameValue) {
        supplierContactName.fill(supplierContactNameValue);
    }
    public void setSupplierContactEmail(String supplierContactEmailValue) {
        supplierContactEmail.fill(supplierContactEmailValue);
    }
    public void setSupplierContactPhone(String supplierContactPhoneValue) {
        supplierContactPhone.type(supplierContactPhoneValue);
    }
    public void clickSaveButton() {
        saveButton.click();
    }


}
