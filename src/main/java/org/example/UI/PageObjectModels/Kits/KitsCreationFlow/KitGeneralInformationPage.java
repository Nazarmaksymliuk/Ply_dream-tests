package org.example.UI.PageObjectModels.Kits.KitsCreationFlow;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
public class KitGeneralInformationPage {
    private final Page page;

    // === Locators (by placeholder) ===
    private final Locator kitNameInput;
    private final Locator kitDescriptionInput;
    private final Locator tagsInput;
    private final Locator nextButton;
    private final Locator saveButton;

    public KitGeneralInformationPage(Page page) {
        this.page = page;

        kitNameInput        = page.getByPlaceholder("Enter kit name");
        kitDescriptionInput = page.getByPlaceholder("Enter kit description");
        tagsInput           = page.locator("input[class='react_select__input']");

        nextButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Next"));
        saveButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save"));
    }

    /** Set required kit name */
    public void setKitName(String name) {
        kitNameInput.click();
        kitNameInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        kitNameInput.fill(name);
    }

    /** Set optional description */
    public void setDescription(String description) {
        kitDescriptionInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        kitDescriptionInput.fill(description);
    }

    /**
     * Add a tag. If the control is a combobox with suggestions,
     * pressing Enter will confirm the current value.
     */
    public void setTag(String tag) {
        tagsInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        tagsInput.fill(tag);
        page.waitForTimeout(1000);
        page.keyboard().press("Enter");
    }

    /** Convenience: fill all visible fields */
    public void fillGeneralInfo(String name, String description) {
        setKitName(name);
        setDescription(description);
    }

    /** Go to the next step of the wizard */
    public KitStockSetupPage clickNext() {
        nextButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        nextButton.click();
        return new KitStockSetupPage(page);
    }
    public void clickSave() {
        saveButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        saveButton.click();
    }

}
