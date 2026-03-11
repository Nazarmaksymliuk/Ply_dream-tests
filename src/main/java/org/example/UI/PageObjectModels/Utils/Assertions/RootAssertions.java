package org.example.UI.PageObjectModels.Utils.Assertions;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import org.example.UI.PageObjectModels.Utils.Waits.WaitUtils;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class RootAssertions {

    /**
     * Перевіряє, що на #root з'явився текст, який відповідає poNumber.
     * Метод можна використовувати будь-де.
     */
    public static void assertTextOnRoot(Page page, String text) {
        // чекаємо, поки текст з'явиться
        WaitUtils.waitForText(page, text);

        // перевірка через AssertJ
        Locator root = page.locator("#root");
        assertThat(root).containsText(text);
    }
}