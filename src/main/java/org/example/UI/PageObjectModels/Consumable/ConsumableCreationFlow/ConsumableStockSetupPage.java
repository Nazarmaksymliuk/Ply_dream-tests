package org.example.UI.PageObjectModels.Consumable.ConsumableCreationFlow;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.example.UI.PageObjectModels.Utils.LocationSelect;

import java.util.List;

public class ConsumableStockSetupPage {
    private final Page page;

    // === Locators ===
    private final Locator addWarehouseButton;
    private final Locator chooseLocationButton;
    private final Locator chooseInListLocationButton;
    private final Locator warehouseDropdown;
    private final Locator warehouseInput;
    private final Locator warehouseOptions;
    private final Locator quantityInput;
    private final Locator saveWarehouseButton;
    private final Locator saveButton;
    private final Locator errorMessageToast;
    private final LocationSelect locationSelectByEnter;


    // === Constructor ===
    public ConsumableStockSetupPage(Page page) {
        this.page = page;

        addWarehouseButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Warehouse"));
        chooseLocationButton = page.locator("div.react_select__input-container.css-ackcql");
        chooseInListLocationButton = page.locator("[class^='react-select__menu-portal']");
        warehouseDropdown = page.locator("div.react_select__control");
        warehouseInput = page.locator("div.react_select__input-container input");
        warehouseOptions = page.locator("div.react_select__menu-list div.react_select__option");
        quantityInput = page.locator("input[placeholder='Enter quantity (can be decimals)']");
        saveWarehouseButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save Warehouse"));
        saveButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save").setExact(true));
        errorMessageToast = page.locator("div.Toastify__toast-body div:nth-child(2)");

        locationSelectByEnter = new LocationSelect(page);

    }

    // === Actions ===
    public void clickAddWarehouseButton() {
        addWarehouseButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        addWarehouseButton.click();
    }

    public void setWarehouseUsingUtility(String warehouseName){
        locationSelectByEnter.setLocationByEnter(warehouseName);
    }

    public void clickChooseLocationButton() {
        chooseLocationButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        chooseLocationButton.click();
    }

    public void clickChooseInListLocationButton() {
        chooseInListLocationButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        chooseInListLocationButton.click();
    }

    public void setQuantity(double quantityValue) {
        quantityInput.scrollIntoViewIfNeeded();
        quantityInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        quantityInput.fill(String.valueOf(quantityValue));
    }

    public void clickSaveWarehouseButton() {
        saveWarehouseButton.scrollIntoViewIfNeeded();
        saveWarehouseButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        saveWarehouseButton.click();
    }

    public void clickSaveButton() {
        saveButton.scrollIntoViewIfNeeded();
        saveButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        saveButton.click();
    }

    /**
     * Select a warehouse from react-select dropdown.
     */
    public void selectWarehouse(String warehouseName) {
        warehouseDropdown.scrollIntoViewIfNeeded();
        warehouseDropdown.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        warehouseDropdown.click();

        warehouseInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        warehouseInput.fill(warehouseName);

        page.waitForSelector("div.react_select__menu"); // wait menu visible

        List<Locator> options = warehouseOptions.all();
        for (Locator option : options) {
            if (option.innerText().contains(warehouseName)) {
                option.click();
                break;
            }
        }
    }

    /**
     * Verify if error toast appears after any action.
     * Throws RuntimeException if error visible.
     */
    public void verifyIfErrorAppears() {
        try {
            errorMessageToast.waitFor(new Locator.WaitForOptions()
                    .setTimeout(5000)
                    .setState(WaitForSelectorState.VISIBLE));
            String message = errorMessageToast.textContent();
            System.out.println("❌ Error appears: " + message);
            throw new RuntimeException("Test stopped due to error toast: " + message);
        } catch (Exception e) {
            System.out.println("✅ No error toast detected");
        }
    }
}
