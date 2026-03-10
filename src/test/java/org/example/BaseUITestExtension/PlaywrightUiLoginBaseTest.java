package org.example.BaseUITestExtension;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.RequestOptions;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.WaitUntilState;
import org.example.BaseUITestExtension.ScreenShoot.ScreenshotManager;
import org.example.UI.PageObjectModels.Authorization.SignIn.SignInPage;
import org.example.config.TestEnvironment;
import org.example.creds.Users;
import org.example.routes.Routes;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class PlaywrightUiLoginBaseTest {

    private static final Logger log = LoggerFactory.getLogger(PlaywrightUiLoginBaseTest.class);

    protected static final String DASHBOARD_PATH = "/dashboard";

    protected Playwright playwright;
    protected Browser browser;
    protected BrowserContext context;
    protected Page page;

    private static final Path STORAGE_STATE_PATH = Paths.get("build/ui-auth-storage-state.json");

    @BeforeAll
    void beforeAll_loginOnceViaUI() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(Boolean.parseBoolean(System.getenv()
                                .getOrDefault("HEADLESS", "false")))
                        .setArgs(List.of(
                                "--disable-dev-shm-usage",
                                "--disk-cache-size=0",
                                "--disable-application-cache"
                        ))
        );

        APIRequestContext api = playwright.request().newContext(
                new APIRequest.NewContextOptions()
                        .setIgnoreHTTPSErrors(true)
        );

        APIResponse resp = api.get(
                Routes.BASE_URL,
                RequestOptions.create().setTimeout(TestEnvironment.HEALTH_CHECK_TIMEOUT_MS)
        );

        log.info("Healthcheck status = {}", resp.status());
        assertTrue(resp.status() < 500, "Stage looks down or unreachable: " + resp.status());
        api.dispose();

        context = browser.newContext(
                new Browser.NewContextOptions()
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
        signIn.signIntoApplication(Users.ADMIN.email(), Users.ADMIN.password());

        page.waitForResponse(
                r -> r.url().contains("/users") && r.status() == 200,
                () -> {}
        );

        Boolean hasPlyUser = (Boolean) page.evaluate("() => !!localStorage.getItem('ply_user')");
        assertTrue(hasPlyUser, "LocalStorage does not contain 'ply_user' after UI login");

        context.storageState(new BrowserContext.StorageStateOptions().setPath(STORAGE_STATE_PATH));
    }

    @AfterAll
    void afterAll_close() {
        if (context != null) context.close();
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @AfterEach
    void takeFinalScreenshot() {
        ScreenshotManager.takeScreenshot(page, "Final screenshot");
    }

    @AfterEach
    void stopTrace(TestInfo info) {
        if (context != null) {
            String safeName = info.getDisplayName()
                    .replaceAll("[^a-zA-Z0-9._-]", "_");
            context.tracing().stop(new Tracing.StopOptions()
                    .setPath(Paths.get("build/traces/" + safeName + ".zip"))
            );
        }
    }

    @BeforeEach
    void startTrace(TestInfo info) {
        if (context != null) {
            context.tracing().start(new Tracing.StartOptions()
                    .setScreenshots(true)
                    .setSnapshots(true)
                    .setSources(true)
            );
        }
    }

    protected Page currentPage() { return page; }

    protected void openPath(String path) {
        page.navigate(
                Routes.BASE_URL + path,
                new Page.NavigateOptions()
                        .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                        .setTimeout(TestEnvironment.NAVIGATION_TIMEOUT_MS)
        );
    }

    protected Page newAuthenticatedPage() { return context.newPage(); }

    protected BrowserContext newAuthenticatedContext() {
        return browser.newContext(new Browser.NewContextOptions()
                .setIgnoreHTTPSErrors(true)
                .setBypassCSP(true)
                .setStorageStatePath(STORAGE_STATE_PATH));
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

    public void waitForDomLoaded() {
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    }

    public void waitForPageStable() {
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }
}
