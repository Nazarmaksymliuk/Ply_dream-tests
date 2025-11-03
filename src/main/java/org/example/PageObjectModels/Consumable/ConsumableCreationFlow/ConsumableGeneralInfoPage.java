package org.example.PageObjectModels.Consumable.ConsumableCreationFlow;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

public class ConsumableGeneralInfoPage {
    private final Page page;

    // === Locators ===
    private final Locator nameInput;
    private final Locator itemNumberInput;
    private final Locator descriptionTextArea;
    private final Locator costForBusinessInput;
    private final Locator tagsInput;

    private final Locator nextButton;
    private final Locator saveButton;

    // === Constructor ===
    public ConsumableGeneralInfoPage(Page page) {
        this.page = page;

        nameInput = page.locator("input[placeholder='Enter Consumable Name']");
        itemNumberInput = page.locator("input[placeholder='Item #']");
        descriptionTextArea = page.locator("textarea[placeholder='Enter Consumable Description']");
        costForBusinessInput = page.locator("input[placeholder='Enter cost']");
        nextButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Next"));
        saveButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save"));

        tagsInput           = page.locator("input[class='react_select__input']").last();

    }

    // === Actions ===
    public void setName(String nameValue) {
        nameInput.click();
        nameInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        nameInput.fill(nameValue);
    }

    public void setItemNumber(String itemNumberValue) {
        itemNumberInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        itemNumberInput.fill(itemNumberValue);
    }

    public void setDescription(String descriptionValue) {
        descriptionTextArea.scrollIntoViewIfNeeded();
        descriptionTextArea.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        descriptionTextArea.fill(descriptionValue);
    }


    public void setCostForBusiness(Double costForBusinessValue) {
        costForBusinessInput.scrollIntoViewIfNeeded();
        costForBusinessInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        costForBusinessInput.fill(String.valueOf(costForBusinessValue));
    }

    public void setTag(String tag) {
        tagsInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        tagsInput.fill(tag);
        page.waitForTimeout(1000);
        page.keyboard().press("Enter");
    }

    public ConsumableStockSetupPage clickNextButton() {
        nextButton.scrollIntoViewIfNeeded();
        nextButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        nextButton.click();
        return new ConsumableStockSetupPage(page);
    }
    public ConsumableStockSetupPage clickSaveButton() {
        saveButton.scrollIntoViewIfNeeded();
        saveButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        saveButton.click();
        return new ConsumableStockSetupPage(page);
    }
}
