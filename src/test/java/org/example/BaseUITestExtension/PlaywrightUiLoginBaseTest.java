package org.example.BaseUITestExtension;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.RequestOptions;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.WaitUntilState;
import org.example.BaseUITestExtension.ScreenShoot.ScreenshotManager;
import org.example.UI.PageObjectModels.Authorization.SignIn.SignInPage;
import org.example.routes.Routes;
import org.junit.jupiter.api.*;
import org.example.routes.Routes.*;


import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class PlaywrightUiLoginBaseTest {
    // === URLs ===
    //protected static final String UI_BASE  = "https://dev.getply.com";
    protected static final String DASHBOARD_PATH = "/dashboard";

    // === Creds (краще через ENV у CI) ===
    protected static final String EMAIL    = System.getenv().getOrDefault("PLY_EMAIL", "maksimlukoleg56@gmail.com");
    protected static final String PASSWORD = System.getenv().getOrDefault("PLY_PASSWORD", "Test+1234");

    // === Shared for suite ===
    protected  Playwright playwright;
    protected  Browser browser;
    protected  BrowserContext context;
    protected  Page page;

    // Збережений стейт (куки + LS) – щоб відкривати нові вкладки/контексти вже залогіненими
    private static final Path STORAGE_STATE_PATH = Paths.get("build/ui-auth-storage-state.json");

    private static final Path STORAGE_STATE = Paths.get("build/ui-auth.json");


    @BeforeAll
    void beforeAll_loginOnceViaUI() {

        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(Boolean.parseBoolean(System.getenv()
                                .getOrDefault("HEADLESS","true")))
                        .setArgs(List.of("--disable-dev-shm-usage","--disk-cache-size=0","--disable-application-cache"))
        );

        APIRequestContext api = playwright.request().newContext(
                new APIRequest.NewContextOptions()
                        .setIgnoreHTTPSErrors(true)
        );

        APIResponse resp = api.get(
                Routes.BASE_URL,
                RequestOptions.create().setTimeout(20_000)
        );

        System.out.println("Healthcheck status = " + resp.status());
        assertTrue(resp.status() < 500, "Stage looks down or unreachable: " + resp.status());

        api.dispose();

        // 1) Чистий контекст
        context = browser.newContext(
                new Browser.NewContextOptions()
                        .setIgnoreHTTPSErrors(true)
                        .setBypassCSP(true)
        );

        context.tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true).setSources(true));

        context.setDefaultTimeout(50_000);
        context.setDefaultNavigationTimeout(50_000);

        page = context.newPage();
        // діагностика на випадок помилок
        page.onResponse(r -> { if (r.status() >= 400) System.out.println("[HTTP " + r.status() + "] " + r.url()); });
        page.onConsoleMessage(m -> System.out.println("[CONSOLE] " + m.type() + " " + m.text()));

        // 2) Перейти на логін і залогінитись через UI
        //page.navigate(UI_BASE, new Page.NavigateOptions().setWaitUntil(WaitUntilState.LOAD));  //MAINNNNNNNN
        page.navigate(Routes.BASE_URL, new Page.NavigateOptions().setWaitUntil(WaitUntilState.COMMIT));

        SignInPage signIn = new SignInPage(page);
        //signIn.signIntoApplication(EMAIL, PASSWORD);         //ПРОТЕ МОЖЕ БУТИ ТУТ , тоді наступна перевірка лишня
        signIn.signIntoApplication(EMAIL, PASSWORD);

        // 3) Переконатись, що сесія піднялась: бек повернув /users та ми бачимо дашборд
        page.waitForResponse(r -> r.url().contains("/users") && r.status() == 200,   ///MAINNNNNNNN
                () ->
                {});


        // наприклад: page.getByText("Welcome").waitFor();
        // або просто перейти на /dashboard і дочекатись завантаження:
        //page.navigate(UI_BASE, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

        // 4) Перевірка, що у LS є ключі (значить фронт нас визнав)
        Boolean hasPlyUser = (Boolean) page.evaluate("() => !!localStorage.getItem('ply_user')");
        assertTrue(hasPlyUser, "LocalStorage does not contain 'ply_user' after UI login");

        // 5) Зберегти стейт (куки + LS) для повторного використання
        context.storageState(new BrowserContext.StorageStateOptions().setPath(STORAGE_STATE_PATH));
        context.tracing().stop(new Tracing.StopOptions().setPath(Paths.get("build/trace.zip")));

    }

    @AfterAll
    void afterAll_close() {
        if (context != null) context.close();
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }
    protected static boolean shouldDeleteStorage = true;

    @AfterEach
    void takeFinalScreenshot(){
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

    //@BeforeAll
    void loginOnceMain() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false));

        // Якщо state вже є — просто використовуй його
        if (java.nio.file.Files.exists(STORAGE_STATE)) {
            context = browser.newContext(new Browser.NewContextOptions()
                    .setIgnoreHTTPSErrors(true)
                    .setBypassCSP(true)
                    .setStorageStatePath(STORAGE_STATE));
            page = context.newPage();
            return;
        }

        // Інакше — один UI-логін
        context = browser.newContext(new Browser.NewContextOptions()
                .setIgnoreHTTPSErrors(true)
                .setBypassCSP(true));
        page = context.newPage();

        context.setDefaultTimeout(150_000);
        context.setDefaultNavigationTimeout(150_000);

        page.navigate(Routes.BASE_URL, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        new SignInPage(page).signIntoApplication(EMAIL, PASSWORD);

        // чекати стан застосунку (без мережевих флейків)
        page.waitForURL(u -> !u.toString().contains("/users"),
                new Page.WaitForURLOptions().setTimeout(60_000));

        // зберегти state на майбутні запуски в цьому ранi
        context.storageState(new BrowserContext.StorageStateOptions().setPath(STORAGE_STATE));
    }

    //@BeforeAll
    void loginOnce() throws IOException {
        playwright = Playwright.create();

        // === читаємо прапор з системних властивостей або змінних середовища ===
        boolean headless = Boolean.parseBoolean(
                System.getProperty("headless",  // Maven: -Dheadless=true
                        System.getenv().getOrDefault("PLAYWRIGHT_HEADLESS", "false"))
        );

        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false));

        if (java.nio.file.Files.exists(STORAGE_STATE)) {
            context = browser.newContext(new Browser.NewContextOptions()
                    .setIgnoreHTTPSErrors(true)
                    .setBypassCSP(true)
                    .setStorageStatePath(STORAGE_STATE));
            page = context.newPage();

            // health-check
            page.navigate(
                    Routes.BASE_URL,
                    new Page.NavigateOptions()
                            .setWaitUntil(WaitUntilState.DOMCONTENTLOADED) // або NETWORKIDLE, якщо потрібно
                            .setTimeout(5000_000) // 60 секунд = 1 хв
            );

            if (waitUntilDashboard(page, 20_000)) {
                // ✅ ми на дашборді: state валідний, рухаємось далі
                page.waitForLoadState(LoadState.NETWORKIDLE); // стабілізація SPA
                return;
            }

            page.waitForLoadState(LoadState.DOMCONTENTLOADED);

            // якщо редіректить на логін або бачимо помилковий тост — робимо релоґін
            boolean needsRelogin =
                    page.url().contains("/sign-in") ||
                            page.getByText("Refresh token does not exist!").isVisible()
                            ||  page.getByText("Welcome back, sign in!").isVisible()
                    || page.getByText("Oops, something went wrong").isVisible(); // якщо тост уже є

            if (!needsRelogin) return;

            // інакше – закриваємо контекст і робимо чистий логін
            try { context.close(); } catch (Throwable ignored) {}
            java.nio.file.Files.deleteIfExists(STORAGE_STATE);
        }

        // clean login
        context = browser.newContext(new Browser.NewContextOptions()
                .setIgnoreHTTPSErrors(true)
                .setBypassCSP(true));
        page = context.newPage();

        page.navigate(
                Routes.BASE_URL,
                new Page.NavigateOptions()
                        .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                        .setTimeout(5000_000)
        );
        new SignInPage(page).signIntoApplication(EMAIL, PASSWORD);
        // дочекатись, що ми НЕ на сторінці логіна, і UI стабільний

        // 2) Чекаємо саме появу /dashboard (а не заперечення)
        page.waitForURL("**/dashboard", new Page.WaitForURLOptions().setTimeout(1000_000));

        //page.waitForLoadState(LoadState.NETWORKIDLE);
        //page.waitForLoadState(LoadState.DOMCONTENTLOADED);


        // зберегти свіжий state (куки + LS з правильного origin)
        context.storageState(new BrowserContext.StorageStateOptions().setPath(STORAGE_STATE));
    }


    //@AfterAll
    static void cleanupStorageState() {          //CLEARS FILE AFTER EVERYTHING IS DONE
        if (shouldDeleteStorage) {
            try {
                java.nio.file.Files.deleteIfExists(STORAGE_STATE);
                System.out.println("[INFO] Deleted STORAGE_STATE after full test suite.");
            } catch (Exception e) {
                System.out.println("[WARN] Failed to delete STORAGE_STATE: " + e.getMessage());
            }
        }
    }


    // ===== Helpers для тестів =====

    /** Поточна сторінка вже залогінена (той самий контекст). */
    protected Page currentPage() { return page; }

    /** Відкрити шлях у поточній сторінці. */
    protected void openPath(String path) {
        page.navigate(
                Routes.BASE_URL + path,
                new Page.NavigateOptions()
                        .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                        .setTimeout(100_000) // ← 120 секунд
        );
    }

    /** Нова вкладка у поточному залогіненому контексті. */
    protected Page newAuthenticatedPage() { return context.newPage(); }

    /** Новий незалежний контекст, уже залогінений за рахунок збереженого storageState. */
    protected BrowserContext newAuthenticatedContext() {
        return browser.newContext(new Browser.NewContextOptions()
                .setIgnoreHTTPSErrors(true)
                .setBypassCSP(true)
                .setStorageStatePath(STORAGE_STATE_PATH));
    }

    /** Проста утиліта: логнути всі 4xx/5xx відповіді вашого бекенду (можеш викликати у тестах). */
    protected void attachErrorLogger(BrowserContext ctx) {
        ctx.onRequestFinished(req -> {
            Response res = req.response();
            if (res != null && res.status() >= 400 && req.url().startsWith(Routes.BASE_URL)) {
                System.out.println("[ERR] " + res.status() + " " + req.url());
                try { System.out.println("      " + res.text()); } catch (Exception ignored) {}
            }
        });
    }



    public void waitForElementPresent(String elementName) {
        page.getByText(elementName).first().waitFor(
                new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(15000)
        );
    }

    public void waitForElementRemoved(String elementName) {
        page.getByText(elementName).waitFor(
                new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.DETACHED)
                        .setTimeout(15000)
        );
    }

    public void waitForDomLoaded() {
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    }

    public void waitForPageStable() {
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }



    /** Повертає true, якщо протягом timeout ми опинились на /dashboard. */
    private static boolean waitUntilDashboard(Page page, int timeoutMs) {
        try {
            page.waitForURL(u -> u.toString().contains("/dashboard"),
                    new Page.WaitForURLOptions().setTimeout(timeoutMs));
            return true;
        } catch (PlaywrightException e) {
            return false;
        }
    }



}
