package org.example.BaseUIApiExtension;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;
import org.example.BaseAPITestExtension.BaseApiTest;
import org.example.UI.PageObjectModels.Authorization.SignIn.SignInPage;
import org.example.config.TestEnvironment;
import org.example.creds.Users;
import org.example.routes.Routes;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Combined UI + API base test.
 * <p>
 * Inherits API contexts ({@code userApi}, {@code adminApi}) from {@link BaseApiTest}.
 * Adds browser automation (Playwright) with UI login.
 * </p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class PlaywrightUiApiBaseTest extends BaseApiTest {

    private static final Logger log = LoggerFactory.getLogger(PlaywrightUiApiBaseTest.class);

    protected static final String DASHBOARD_PATH = "/dashboard";

    protected Browser browser;
    protected BrowserContext context;
    protected Page page;

    @BeforeAll
    void beforeAll_setupUi() throws IOException {
        // BaseApiTest.setUp() already ran and set up userApi, adminApi, playwright

        boolean headless = Boolean.parseBoolean(
                System.getProperty("headless",
                        System.getenv().getOrDefault("HEADLESS", "true"))
        );

        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(headless)
                        .setArgs(List.of(
                                "--disable-dev-shm-usage",
                                "--disk-cache-size=0",
                                "--disable-application-cache"
                        ))
        );

        context = browser.newContext(new Browser.NewContextOptions()
                .setIgnoreHTTPSErrors(true)
                .setBypassCSP(true)
        );

        context.setDefaultTimeout(TestEnvironment.DEFAULT_TIMEOUT_MS);
        context.setDefaultNavigationTimeout(TestEnvironment.DEFAULT_TIMEOUT_MS);

        page = context.newPage();

        page.onResponse(r -> {
            if (r.status() >= 400) log.warn("[HTTP {}] {}", r.status(), r.url());
        });
        page.onConsoleMessage(m -> log.debug("[CONSOLE] {} {}", m.type(), m.text()));

        page.navigate(Routes.BASE_URL, new Page.NavigateOptions().setWaitUntil(WaitUntilState.COMMIT));

        SignInPage signIn = new SignInPage(page);
        page.waitForResponse(
                r -> r.url().contains("/users") && r.status() == 200,
                () -> signIn.signIntoApplication(Users.ADMIN.email(), Users.ADMIN.password())
        );

        page.waitForURL(u -> !u.toString().contains("/sign-in"),
                new Page.WaitForURLOptions().setTimeout(TestEnvironment.NAVIGATION_TIMEOUT_MS));
    }

    @AfterAll
    void afterAll_closeBrowser() {
        if (context != null) context.close();
        if (browser != null) browser.close();
        // playwright and API contexts are closed by BaseApiTest.tearDown()
    }

    protected void openPath(String path) {
        page.navigate(
                Routes.BASE_URL + path,
                new Page.NavigateOptions()
                        .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                        .setTimeout(TestEnvironment.NAVIGATION_TIMEOUT_MS)
        );
    }

    public void waitForElementPresent(String elementName) {
        page.getByText(elementName).first().waitFor(
                new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(TestEnvironment.ELEMENT_WAIT_TIMEOUT_MS)
        );
    }

    public void waitForElementRemoved(String elementName) {
        page.getByText(elementName).waitFor(
                new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.DETACHED)
                        .setTimeout(TestEnvironment.ELEMENT_WAIT_TIMEOUT_MS)
        );
    }
}
