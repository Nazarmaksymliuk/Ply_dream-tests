package org.example.PageObjectModels.Catalog.MaterialsTab;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

public class PriceAndVariantsPage {
    private final Page page;

    // === Locators ===
    private final Locator costForClientInput;
    private final Locator costForBusinessInput;
    private final Locator nextButton;

    // === Constructor ===
    public PriceAndVariantsPage(Page page) {
        this.page = page;

        costForClientInput = page.locator("(//input[@placeholder='Enter cost'])[1]");
        costForBusinessInput = page.locator("(//input[@placeholder='Enter cost'])[2]");
        nextButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Next"));
    }

    // === Actions ===

    public void setCostForClient(Double costForClientValue) {
        costForClientInput.waitFor(
                new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE)
        );
        costForClientInput.fill(String.valueOf(costForClientValue));
    }

    public void setCostForBusiness(Double costForBusinessValue) {
        costForBusinessInput.waitFor(
                new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE)
        );
        costForBusinessInput.fill(String.valueOf(costForBusinessValue));
    }

    public StockSetupPage clickNextButton() {
        nextButton.waitFor(
                new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE)
        );
        nextButton.click();
        return new StockSetupPage(page);
    }

}
