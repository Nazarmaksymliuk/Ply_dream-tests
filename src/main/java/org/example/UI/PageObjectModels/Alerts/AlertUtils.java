package org.example.UI.PageObjectModels.Alerts;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

import org.example.config.TestEnvironment;

import java.util.regex.Pattern;

public class AlertUtils {
    public static String getAlertText(Page page) {
        Locator alert = page.locator("[role='alert']");
        alert.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(TestEnvironment.ELEMENT_WAIT_TIMEOUT_MS));

        return alert.innerText().trim();
    }
    public static String getAlertTextWithToast(Page page) {
        Locator successToast = page
                .locator("div.Toastify__toast-body[role='alert']");

        return successToast.innerText().trim();
    }

    public static void waitForAlertHidden(Page page) {
        Locator alert = page.locator("[role='alert']");
        alert.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.HIDDEN)
                .setTimeout(TestEnvironment.DIALOG_TIMEOUT_MS));
    }
    public static void waitForAlertHiddenWithToast(Page page) {
        Locator successToast = page
                .locator("div.Toastify__toast-body[role='alert']");
        successToast.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.HIDDEN)
                .setTimeout(TestEnvironment.DIALOG_TIMEOUT_MS));
    }
    public static void waitForAlertVisible(Page page) {
        page.waitForTimeout(TestEnvironment.SMALL_DELAY_MS);
        Locator alert = page.locator("[role='alert']");
        alert.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(TestEnvironment.DIALOG_TIMEOUT_MS));
    }
    public static void waitForAlertVisibleWithToast(Page page) {
        page.waitForTimeout(TestEnvironment.SMALL_DELAY_MS);
        Locator successToast = page
                .locator("div.Toastify__toast-body[role='alert']");

    }



}
