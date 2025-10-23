package org.example.PageObjectModels.Alerts;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

public class AlertUtils {
    public static String getAlertText(Page page) {
        Locator alert = page.locator("[role='alert']");
        alert.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(15000));
        return alert.innerText().trim();
    }

    public static void waitForAlertHidden(Page page) {
        Locator alert = page.locator("[role='alert']");
        alert.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.HIDDEN)
                .setTimeout(15000));
    }
    public static void waitForAlertVisible(Page page) {
        Locator alert = page.locator("[role='alert']");
        alert.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(15000));
    }
}
