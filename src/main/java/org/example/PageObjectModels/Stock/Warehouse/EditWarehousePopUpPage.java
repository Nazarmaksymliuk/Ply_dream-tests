package org.example.PageObjectModels.Stock.Warehouse;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

public class EditWarehousePopUpPage {
    private final Page page;

    // === Locators ===
    private final Locator warehouseNameInput;
    private final Locator warehouseAddressInput;
    private final Locator warehouseAptInput;
    private final Locator warehouseCityInput;
    private final Locator warehouseZipInput;
    private final Locator warehouseStateDropdown;
    private final Locator saveChangesButton;

    // === Constructor ===
    public EditWarehousePopUpPage(Page page) {
        this.page = page;

        // Використовуємо getByPlaceholder — найкраща практика Playwright
        warehouseNameInput = page.getByPlaceholder("Enter warehouse name");
        warehouseAddressInput = page.getByPlaceholder("Enter address");
        warehouseAptInput = page.getByPlaceholder("#123");
        warehouseCityInput = page.getByPlaceholder("Enter city");
        warehouseZipInput = page.getByPlaceholder("Zip Code");

        // Dropdown “State / Province”
        warehouseStateDropdown = page.getByText("State / Province");

        // Кнопка “Save changes”
        saveChangesButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Save changes"));
    }

    // === Actions ===
    public void setWarehouseName(String name) {
        warehouseNameInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        warehouseNameInput.fill(name);
    }

    public void setWarehouseAddress(String address) {
        warehouseAddressInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        warehouseAddressInput.fill(address);
    }

    public void setWarehouseApt(String apt) {
        warehouseAptInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        warehouseAptInput.fill(apt);
    }

    public void setWarehouseCity(String city) {
        warehouseCityInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        warehouseCityInput.fill(city);
    }

    public void setWarehouseZip(String zip) {
        warehouseZipInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        warehouseZipInput.fill(zip);
    }

    public void setWarehouseState(String state) {
        warehouseStateDropdown.scrollIntoViewIfNeeded();
        warehouseStateDropdown.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        warehouseStateDropdown.click();

        // вибрати штат по тексту (перший збіг)
        page.locator(".react_select__option")
                .filter(new Locator.FilterOptions().setHasText(state))
                .first()
                .click();
    }

    public void clickSaveChangesButton() {
        saveChangesButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        saveChangesButton.click();
    }

    // (опціонально) чек завантаження сторінки
    public void waitForLoaded() {
        warehouseNameInput.waitFor(
                new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(15000)
        );
    }
}
