package org.example.UI.PageObjectModels.Stock.Trucks;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

public class TruckFormPopUpPage {
    private final Page page;

    // === Locators ===
    private final Locator nameInput;
    private final Locator plateInput;
    private final Locator makeInput;
    private final Locator modelInput;
    private final Locator addTruckButton;
    private final Locator saveChangesButton;

    private final Locator dialogRoot;

    public TruckFormPopUpPage(Page page) {
        this.page = page;

        dialogRoot = page.locator("body"); // або знайди data-testid, якщо є свій контейнер

        nameInput  = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Enter truck name"));
        plateInput = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Enter license plate number"));
        makeInput  = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Enter truck make"));
        modelInput = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Enter truck model"));

        // одна кнопка використовується і для створення ("Add Truck"), і для збереження ("Save changes")
        addTruckButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Truck"));
        saveChangesButton = page.getByText("Save changes");
    }

    public void waitForLoaded() {
        dialogRoot.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(15000));
    }

    public void setTruckName(String value)  { nameInput.fill(value); }
    public void setTruckPlate(String value) { plateInput.fill(value); }
    public void setTruckMake(String value)  { makeInput.fill(value); }
    public void setTruckModel(String value) { modelInput.fill(value); }

    public void clickAddTruckButton() {
        addTruckButton.click();
    }
    public void clickSaveChangesButton(){
        saveChangesButton.click();
    }
}
