package org.example.UI.PageObjectModels.Utils.Waits;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * Універсальний клас для очікувань елементів на сторінках.
 */
public class WaitUtils {

    private static final int DEFAULT_TIMEOUT = 15_000;
    private static final String LOADER_SELECTOR = "img[alt='loader']";

    // =========================
    // BASIC WAITS
    // =========================

    public static void waitForVisible(Locator locator, int timeoutMs) {
        locator.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(timeoutMs));
    }

    public static void waitForVisible(Locator locator) {
        waitForVisible(locator, DEFAULT_TIMEOUT);
    }

    public static void waitForAttached(Locator locator) {
        locator.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.ATTACHED)
                .setTimeout(DEFAULT_TIMEOUT));
    }

    public static void waitForHidden(Locator locator) {
        // ⚠️ У strict mode locator.waitFor впаде, якщо матчиться >1 елемент.
        // Тому цей метод краще викликати для унікальних локаторів.
        locator.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.HIDDEN)
                .setTimeout(DEFAULT_TIMEOUT));
    }

    // =========================
    // LOADER WAITS
    // =========================

    /**
     * Очікує, що всі лоадери (img[alt='loader']) зникнуть (hidden або removed).
     * Не падає у strict mode, навіть якщо лоадерів кілька.
     */
    public static void waitForLoaderToDisappear(Page page) {
        // якщо лоадера взагалі нема — не чекаємо
        if (page.locator(LOADER_SELECTOR).count() == 0) return;

        page.waitForSelector(
                LOADER_SELECTOR,
                new Page.WaitForSelectorOptions()
                        .setState(WaitForSelectorState.HIDDEN) // hidden або detached під капотом
                        .setTimeout(DEFAULT_TIMEOUT)
        );
    }

    /**
     * Якщо хочеш чекати саме "елементів нема в DOM" (count == 0).
     * Корисно, коли лоадер не ховається, а прибирається з DOM.
     */
    public static void waitForLoaderDetached(Page page) {
        page.waitForFunction(
                "selector => document.querySelectorAll(selector).length === 0",
                LOADER_SELECTOR,
                new Page.WaitForFunctionOptions().setTimeout(DEFAULT_TIMEOUT)
        );
    }
}
