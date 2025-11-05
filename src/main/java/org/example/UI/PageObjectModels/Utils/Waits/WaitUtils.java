package org.example.UI.PageObjectModels.Utils.Waits;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * Універсальний клас для очікувань елементів на сторінках.
 * Підходить для будь-якого гріда / таблиці / списку.
 */
public class WaitUtils {

    /**
     * Очікує, що елемент з’явиться і стане видимим на сторінці.
     *
     * @param locator   — Playwright Locator (елемент, який потрібно дочекатись)
     * @param timeoutMs — тривалість очікування у мілісекундах (за замовчуванням 15 секунд)
     */
    public static void waitForVisible(Locator locator, int timeoutMs) {
        locator.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(timeoutMs));
    }

    /**
     * Спрощена версія з дефолтним таймаутом у 15 секунд.
     *
     * @param locator — Playwright Locator (елемент, який потрібно дочекатись)
     */
    public static void waitForVisible(Locator locator) {
        waitForVisible(locator, 15_000);
    }

    /**
     * Очікує, що елемент буде присутній у DOM (але може бути невидимим).
     *
     * @param locator — Playwright Locator (елемент)
     */
    public static void waitForAttached(Locator locator) {
        locator.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.ATTACHED)
                .setTimeout(15_000));
    }

    /**
     * Очікує, що елемент зникне зі сторінки.
     *
     * @param locator — Playwright Locator (елемент, який має зникнути)
     */
    public static void waitForHidden(Locator locator) {
        locator.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.HIDDEN)
                .setTimeout(15_000));
    }
}
