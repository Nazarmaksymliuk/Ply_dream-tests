package org.example.UI.PageObjectModels.Material.MaterialEditAvailabilityFlow;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

public class MaterialEditAvailabilityPopUpPage {
    private final Page page;

    // Корінь діалогу (для стабільного очікування)
    private final Locator dialogRoot;

    // Поля (за плейсхолдерами з твого опису)
    private final Locator quantityInput;   // "Enter quantity"
    private final Locator minAmountInput;  // "Enter min amount"
    private final Locator maxAmountInput;  // "Enter max amount"

    // Кнопки
    private final Locator saveChangesButton;
    private final Locator cancelButton;

    public MaterialEditAvailabilityPopUpPage(Page page) {
        this.page = page;

        dialogRoot = page.getByRole(AriaRole.DIALOG); // загальний діалог
        quantityInput  = page.getByPlaceholder("Enter quantity");
        minAmountInput = page.getByPlaceholder("Enter min amount");
        maxAmountInput = page.getByPlaceholder("Enter max amount");

        saveChangesButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save Changes"));
        cancelButton      = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Cancel"));
    }

    public void waitForLoaded() {
        dialogRoot.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(20_000));
        saveChangesButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public void setQuantity(Integer qty) {
        quantityInput.clear();
        quantityInput.fill(String.valueOf(qty));
    }

    public void setMinAmount(int min) {
        minAmountInput.clear();
        minAmountInput.fill(Integer.toString(min));
    }

    public void setMaxAmount(int max) {
        maxAmountInput.clear();
        maxAmountInput.fill(Integer.toString(max));
    }

    public void clickSaveChanges() {
        saveChangesButton.click();
    }

    public void clickCancel() {
        cancelButton.click();
    }
}
