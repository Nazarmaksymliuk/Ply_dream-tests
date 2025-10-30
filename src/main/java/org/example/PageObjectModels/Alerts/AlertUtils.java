package org.example.PageObjectModels.Alerts;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.util.regex.Pattern;

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
                .setTimeout(20000));
    }
    public static void waitForAlertVisible(Page page) {
        Locator alert = page.locator("[role='alert']");
        alert.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(20000));
    }



}
