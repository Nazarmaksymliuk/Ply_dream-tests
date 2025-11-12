package org.example.UI.PageObjectModels.Transfer;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

public class TransferPage {
    private final Page page;

    // === Локатори ===
    private final Locator firstRowQtyButton;       // кнопка “X Each” у першому рядку
    private final Locator firstRowCheckbox;        // чекбокс першого рядка
    private final Locator transferToolbarButton;   // кнопка "Transfer"
    private final Locator moveToLocationButton;    // кнопка "Move To Location"
    private final Locator moveToJobButton;    // кнопка "Move To Location"

    public TransferPage(Page page) {
        this.page = page;

        // Кнопка кількості у першому рядку — шукаємо div[role='button'], який містить span[aria-label='Each']
        firstRowQtyButton = page.locator("div[role='button']:has(span[aria-label='Each'])").first();

        // Чекбокс першого рядка — беремо другий, бо перший зазвичай “Select all”
        firstRowCheckbox = page.getByRole(AriaRole.CHECKBOX).nth(1);

        // Кнопка Transfer у тулбарі
        transferToolbarButton = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Transfer"));

        // Кнопка Move To Location у тулбарі
        moveToLocationButton = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Move To Location"));

        moveToJobButton = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Move To Job"));


    }

    /** Очікує, що грід готовий і перший рядок видимий */
    public void waitForReady() {
        firstRowQtyButton.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(15_000));
    }

    /** Клік по чекбоксу першого рядка */
    public void checkFirstRow() {
        firstRowCheckbox.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(10_000));
        firstRowCheckbox.check();
    }

    /** Відкрити модалку Transfer (через тулбар) */
    public TransferModalPage clickTransferButton() {
        transferToolbarButton.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(10_000));
        transferToolbarButton.click();
        return new TransferModalPage(page);
    }

    /** Відкрити модалку Move To Location (через тулбар) */
    public TransferModalPage clickMoveToLocationButton() {
        moveToLocationButton.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(10_000));
        moveToLocationButton.click();
        return new TransferModalPage(page);
    }
    public TransferModalPage clickMoveToJobButton() {
        moveToJobButton.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(10_000));
        moveToJobButton.click();
        return new TransferModalPage(page);
    }
}
