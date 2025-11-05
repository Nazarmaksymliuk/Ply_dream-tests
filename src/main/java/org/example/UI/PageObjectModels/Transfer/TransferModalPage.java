package org.example.UI.PageObjectModels.Transfer;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.example.UI.PageObjectModels.Utils.LocationSelect;

public class TransferModalPage {
    private final Page page;

    private final Locator dialogRoot;

    // react-select (location)
    private final Locator locationInputContainer; // клік — з’являється інпут
    private final Locator locationSearchInput;    // власне інпут у react-select

    // qty у модалці (spinbutton)
    private final Locator qtySpinButton;

    private final Locator transferButton; // кнопка Transfer у модалці
    private final Locator confirmButton;  // підтвердження після трансферу

    private final LocationSelect locationSelect;

    private final Locator moveToolButton;  // “Move 1 Tool”


    public TransferModalPage(Page page) {
        this.page = page;

        locationSelect = new LocationSelect(page);

        dialogRoot = page.getByRole(AriaRole.DIALOG);

        // "Choose Location" — react-select
        locationInputContainer = page.locator(".react_select__input-container").first();
        // сам input всередині react-select (id змінний → ловимо по [id^='react-select-'][id$='-input'])
        locationSearchInput = page.locator("input[id^='react-select-'][id$='-input']").first();

        // перший spinbutton у модалці — кількість до трансферу
        qtySpinButton = page.getByRole(AriaRole.SPINBUTTON).first();

        transferButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Transfer"));
        confirmButton  = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Confirm"));
        moveToolButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Move 1 Tool"));


    }

    /** Чекає, поки модалка з’явиться */
    public void waitForLoaded() {
        dialogRoot.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(20_000));
    }

    /** Обирає локацію для трансферу (через react-select) */
    public void selectLocation(String locationName) {
        locationSelect.setLocationByEnter(locationName);
    }

    /** Встановлює кількість для трансферу */
    public void setTransferQuantity(int qty) {
        qtySpinButton.click();
        qtySpinButton.fill(Integer.toString(qty));
    }

    /** Натискає кнопку Transfer у модалці */
    public void clickTransfer() {
        transferButton.click();
    }

    public void clickMoveTool() {
        moveToolButton.click();
    }

    /** Натискає кнопку Confirm у підтвердженні */
    public void clickConfirm() {
        confirmButton.click();
    }
}
