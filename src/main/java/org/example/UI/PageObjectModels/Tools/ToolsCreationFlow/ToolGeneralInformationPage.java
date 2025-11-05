package org.example.UI.PageObjectModels.Tools.ToolsCreationFlow;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

public class ToolGeneralInformationPage {
    private final Page page;
    // === Локатори за placeholder ===
    private final Locator toolNameInput;
    private final Locator mfgNumberInput;
    private final Locator toolDescriptionInput;
    private final Locator tagsInput;
    private final Locator nextButton;


    public ToolGeneralInformationPage(Page page) {
        this.page = page;

        toolNameInput = page.getByPlaceholder("Enter tool name");
        mfgNumberInput = page.getByPlaceholder("Enter MFG #");
        toolDescriptionInput = page.getByPlaceholder("Enter tool description");
        tagsInput = page.locator("[class='react_select__input']");
        nextButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Next"));


    }

    public void setToolName(String name) {
        toolNameInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        toolNameInput.fill(name);
    }

    public void setMfgNumber(String mfgNumber) {
        mfgNumberInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        mfgNumberInput.fill(mfgNumber);
    }

    public void setToolDescription(String description) {
        toolDescriptionInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        toolDescriptionInput.fill(description);
    }

    public void setTags(String tag) {
        tagsInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        tagsInput.fill(tag);
        // Якщо теги обираються зі списку — можеш додати:
        page.keyboard().press("Enter");
    }

    public AddEditUnitsPage clickNextPage(){
        nextButton.click();
        return new AddEditUnitsPage(page);
    }


}
