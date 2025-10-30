package org.example.BaseTestExtension;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.WaitUntilState;
import org.example.PageObjectModels.Authorization.SignIn.SignInPage;
import org.junit.jupiter.api.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class PlaywrightUiLoginBaseTest {
    // === URLs ===
    protected static final String UI_BASE  = "https://stage.getply.com";
    protected static final String DASHBOARD_PATH = "/dashboard";

    // === Creds (краще через ENV у CI) ===
    protected static final String EMAIL    = System.getenv().getOrDefault("PLY_EMAIL", "maksimlukoleg56@gmail.com");
    protected static final String PASSWORD = System.getenv().getOrDefault("PLY_PASSWORD", "Test+1234");

    // === Shared for suite ===
    protected Playwright playwright;
    protected Browser browser;
    protected BrowserContext context;
    protected Page page;

    // Збережений стейт (куки + LS) – щоб відкривати нові вкладки/контексти вже залогіненими
    private static final Path STORAGE_STATE_PATH = Paths.get("build/ui-auth-storage-state.json");

    //@BeforeAll
    void beforeAll_loginOnceViaUI() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(Boolean.parseBoolean(System.getenv()
                                .getOrDefault("HEADLESS","false")))
                        .setArgs(java.util.List.of("--disable-dev-shm-usage","--disk-cache-size=0","--disable-application-cache"))
        );

        // 1) Чистий контекст
        context = browser.newContext(
                new Browser.NewContextOptions()
                        .setIgnoreHTTPSErrors(true)
                        .setBypassCSP(true)
        );
        context.setDefaultTimeout(350_000);
        context.setDefaultNavigationTimeout(350_000);

        page = context.newPage();
        // діагностика на випадок помилок
        page.onResponse(r -> { if (r.status() >= 400) System.out.println("[HTTP " + r.status() + "] " + r.url()); });
        page.onConsoleMessage(m -> System.out.println("[CONSOLE] " + m.type() + " " + m.text()));

        // 2) Перейти на логін і залогінитись через UI
        //page.navigate(UI_BASE, new Page.NavigateOptions().setWaitUntil(WaitUntilState.LOAD));  //MAINNNNNNNN
        page.navigate(UI_BASE, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

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
    }

    @BeforeAll
    void loginOnce() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(true));

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

        context.setDefaultTimeout(350_000);
        context.setDefaultNavigationTimeout(350_000);

        page.navigate(UI_BASE, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        new SignInPage(page).signIntoApplication(EMAIL, PASSWORD);

        // чекати стан застосунку (без мережевих флейків)
        page.waitForURL(u -> !u.toString().contains("/users"),
                new Page.WaitForURLOptions().setTimeout(60_000));

        // зберегти state на майбутні запуски в цьому ранi
        context.storageState(new BrowserContext.StorageStateOptions().setPath(STORAGE_STATE));
    }

    @AfterAll
    void afterAll_close() {
        if (context != null) context.close();
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    // ===== Helpers для тестів =====

    /** Поточна сторінка вже залогінена (той самий контекст). */
    protected Page currentPage() { return page; }

    /** Відкрити шлях у поточній сторінці. */
    protected void openPath(String path) {
        page.navigate(
                UI_BASE + path,
                new Page.NavigateOptions()
                        .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                        .setTimeout(300_000) // ← 120 секунд
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
            if (res != null && res.status() >= 400 && req.url().startsWith(UI_BASE)) {
                System.out.println("[ERR] " + res.status() + " " + req.url());
                try { System.out.println("      " + res.text()); } catch (Exception ignored) {}
            }
        });
    }



    public void waitForElementPresent(String elementName) {
        page.getByText(elementName).waitFor(
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


    private static final Path STORAGE_STATE = Paths.get("build/ui-auth.json");


}
