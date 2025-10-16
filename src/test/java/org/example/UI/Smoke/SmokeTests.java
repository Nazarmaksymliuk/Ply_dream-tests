package org.example.UI.Smoke;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;
import org.example.BaseTestExtension.PlaywrightBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class SmokeTests extends PlaywrightBaseTest {

    // окремий метод — повертає один з локаторів, які тут вказані
    private Locator pageTitle(Page p) {
        return p.locator(
                "[data-testid='page-title'], " +     // ваш канонічний testid, якщо є
                        ".header_dashboard_title, " +        // те, що вже використовували
                        ".text-base.font-semibold"             // запасні варіанти
        ).first();
    }

    // ✅ джерело даних для параметризованого тесту
    static Stream<Arguments> routes() {
        return Stream.of(
                Arguments.of("/dashboard",       "Dashboard"),
                Arguments.of("/purchase-orders", "Purchase Orders"),
                Arguments.of("/all-requests",    "Purchasing"),
                Arguments.of("/returns",         "Returns"),
                Arguments.of("/suppliers",       "Suppliers"),
                Arguments.of("/catalog",         "Catalog"),
                Arguments.of("/draft-list",      "Draft List"),
                Arguments.of("/library",         "Ply Library"),
                Arguments.of("/stock",           "Stock"),
                Arguments.of("/field-requests",  "Field requests"),
                Arguments.of("/jobs",            "Jobs"),
                Arguments.of("/reports",         "Reports")
        );
    }


    @ParameterizedTest(name = "Smoke: {0} shows \"{1}\" in page title")
    @MethodSource("routes")
    void route_hasExpectedTitle(String path, String expectedTitleSubstring) {
        // контекст і сторінка вже авторизовані завдяки PlaywrightBaseTest
        page.navigate(UI_BASE + path,
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

        // дочекайся заголовка
        Locator title = pageTitle(page);
        assertThat(title).isVisible();

        // текст заголовка містить очікуваний фрагмент
        assertThat(title).containsText(expectedTitleSubstring);
    }
}
