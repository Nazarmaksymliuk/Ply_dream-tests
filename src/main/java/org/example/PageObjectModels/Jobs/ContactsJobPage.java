package org.example.PageObjectModels.Jobs;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class ContactsJobPage {
    private final Page page;

    private final Locator clientName;
    private final Locator clientPhoneNumber;
    private final Locator noteField;
    private final Locator addJobButton;
    private final Locator editJobButton;


    public ContactsJobPage(Page page) {
        this.page = page;
        // ініціалізація локаторів (аналог By у Selenium)
        clientName = page.getByPlaceholder("Enter client's name");
        clientPhoneNumber = page.getByPlaceholder("Enter phone number");
        noteField = page.getByPlaceholder("If necessary, you can leave a note");
        addJobButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Job"));
        editJobButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Edit Job"));
    }

    public void setClientName(String clientNameValue) {
        clientName.fill(clientNameValue);
    }

    public void setClientPhoneNumber(String clientPhoneNumberValue) {
        clientPhoneNumber.click();
        clientPhoneNumber.type(clientPhoneNumberValue);
    }

    public void setNoteField(String noteFieldValue) {
        noteField.fill(noteFieldValue);
    }

    public void clickAddJobButton() {
        addJobButton.click();
    }
    public void clickEditJobButton() {
        editJobButton.click();
    }

    public boolean isAddJobButtonVisible() {
        return addJobButton.isVisible();
    }
}
