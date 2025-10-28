package org.example.PageObjectModels.Material.MaterialsCreationFlow;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.util.List;

public class MaterialStockSetupPage {
    private final Page page;

    // === Locators ===
    private final Locator addLocationButton;
    private final Locator chooseLocationButton;
    private final Locator chooseInListLocationButton;
    private final Locator warehouseDropdown;
    private final Locator warehouseInput;
    private final Locator warehouseOptions;
    private final Locator quantityInput;
    private final Locator saveLocationButton;
    private final Locator saveButton;
    private final Locator errorToast;

    // === Constructor ===
    public MaterialStockSetupPage(Page page) {
        this.page = page;

        addLocationButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Location"));
        chooseLocationButton = page.locator("div.react_select__input-container.css-ackcql");
        chooseInListLocationButton = page.locator("div[class^='react-select__menu-portal']");
        warehouseDropdown = page.locator("div.react_select__control");
        warehouseInput = page.locator("div.react_select__input-container input");
        warehouseOptions = page.locator("div.react_select__menu-list div.react_select__option");
        quantityInput = page.locator("input[placeholder='Enter quantity']");
        saveLocationButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save Location"));
        saveButton = page.locator("[class^=_actions_]").getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Save"));
        errorToast = page.locator("div.Toastify__toast-body div:nth-child(2)");
    }

    // === Actions ===

    public void clickAddLocationButton() {
        addLocationButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        addLocationButton.click();
    }

    public void clickChooseLocationButton() {
        chooseLocationButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        chooseLocationButton.click();
    }

    public void clickChooseInListLocationButton() {
        chooseInListLocationButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        chooseInListLocationButton.click();
    }

    public void setQuantity(Integer quantityValue) {
        quantityInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        quantityInput.fill(String.valueOf(quantityValue));
    }

    public void clickSaveLocationButton() {
        saveLocationButton.scrollIntoViewIfNeeded();
        saveLocationButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        saveLocationButton.click();
    }

    public void clickSaveButton() {
        saveButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        saveButton.click();
    }

    /**
     * Вибирає warehouse із дропдауну, підставляючи текст і клікаючи по збігу.
     */
    public void selectWarehouse(String warehouseName) {
        warehouseDropdown.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        warehouseDropdown.click();

        warehouseInput.fill(warehouseName);

        List<Locator> options = warehouseOptions.all();
        for (Locator option : options) {
            if (option.innerText().contains(warehouseName)) {
                option.click();
                break;
            }
        }
    }
}
