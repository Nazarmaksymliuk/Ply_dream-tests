package org.example.UI.PageObjectModels.Stock.Warehouse;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

public class CreateWarehousePopUpPage {
    private final Page page;

    // === Locators ===
    private final Locator warehouseNameInput;
    private final Locator warehouseAddressInput;
    private final Locator warehouseAptInput;
    private final Locator warehouseCityInput;
    private final Locator warehouseZipInput;

    // React-Select "State / Province"
    private final Locator stateDropdown;            // клік, щоб відкрити список
    private final Locator firstStateOption;         // перша опція у випадаючому списку

    private final Locator addWarehouseButton;
    private final Locator closeDialogButton;        // кнопка закриття модалки (іконка хрестика)

    // === Constructor ===
    public CreateWarehousePopUpPage(Page page) {
        this.page = page;

        warehouseNameInput    = page.locator("input[placeholder='Enter warehouse name'][name='name']");
        warehouseAddressInput = page.locator("input[placeholder='Enter address'][name='address']");
        warehouseAptInput     = page.locator("input[placeholder='#123']");
        warehouseCityInput    = page.locator("input[placeholder='Enter city'][name='city']");
        warehouseZipInput     = page.locator("input[placeholder='Zip Code']");

        stateDropdown = page.getByText("State / Province").nth(1);
        firstStateOption = page.locator(".react_select__option")
                .or(page.locator("div[role='option']:not(.disabled)"))
                .first();
        addWarehouseButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Warehouse"));
        closeDialogButton = page.locator("div.MuiDialog-container div.MuiDialog-paper button.MuiIconButton-root svg[data-testid='CloseIcon']")
                .or(page.locator("div.MuiDialog-container div.MuiDialog-paper button.MuiIconButton-root"));
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

    /** Встановити значення в поле State (якщо там не select, а input з автодоповненням) */
    public void setWarehouseState(String state) {
        setFirstStateForLocation(); // відкриваємо список
        // вибрати опцію за текстом (стабільніше за індекс)
        page.locator(".react_select__option").filter(new Locator.FilterOptions().setHasText(state))
                .first()
                .click();
    }

    /** Клік по дропдауну, щоб відкрити список штатів/провінцій */
    public void setFirstStateForLocation() {
        stateDropdown.scrollIntoViewIfNeeded();
        stateDropdown.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        stateDropdown.click();
        page.keyboard().press("Enter");
    }

    /** Вибрати першу доступну опцію штату/провінції у списку */
    public void chooseFirstWarehouseState() {
        setFirstStateForLocation();
        firstStateOption.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
        firstStateOption.click();
    }

    /** Натиснути Add Warehouse (створити новий склад) */
    public void clickAddWarehouseButton() {
        addWarehouseButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        addWarehouseButton.click();
    }

    /** Закрити модалку перегляду/створення складу */
    public void clickCloseViewWarehouseOptionButton() {
        closeDialogButton.scrollIntoViewIfNeeded();
        closeDialogButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        closeDialogButton.click();
    }


}
