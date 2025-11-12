package org.example.BaseUITestExtension;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
import org.example.UI.PageObjectModels.Authorization.SignIn.SignInPage;
import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class BaseUiTest {

    // === Константи ===
    protected static final String UI_BASE = "https://stage.getply.com";
    protected static final String EMAIL    = System.getenv().getOrDefault("PLY_EMAIL", "maksimlukoleg56@gmail.com");
    protected static final String PASSWORD = System.getenv().getOrDefault("PLY_PASSWORD", "Test+1234");

    // === Шлях до збереженого стейту ===
    private static final Path STORAGE_STATE = Paths.get("build/ui-auth.json");

    // === Спільні об’єкти для всієї тестової сесії ===
    protected Playwright playwright;
    protected Browser browser;

    // === Окремі для кожного тесту ===
    protected BrowserContext context;
    protected Page page;

    //@BeforeAll
    void loginOnceAndSaveState() throws Exception {
        playwright = Playwright.create();

        boolean headless = Boolean.parseBoolean(
                System.getProperty("headless",
                        System.getenv().getOrDefault("PLAYWRIGHT_HEADLESS", "false"))
        );

        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(headless)
                        .setArgs(java.util.List.of("--disable-dev-shm-usage",
                                "--disk-cache-size=0",
                                "--disable-application-cache"))
        );

        // Якщо файл стейту вже існує — не логінимось повторно
        if (Files.exists(STORAGE_STATE)) {
            System.out.println("[INFO] Storage state already exists, skipping login.");
            return;
        }

        // === 1️⃣  Виконуємо логін через UI один раз ===
        BrowserContext loginContext = browser.newContext(new Browser.NewContextOptions()
                .setIgnoreHTTPSErrors(true)
                .setBypassCSP(true)
        );
        Page loginPage = loginContext.newPage();

        loginPage.navigate(UI_BASE, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        new SignInPage(loginPage).signIntoApplication(EMAIL, PASSWORD);

        loginPage.waitForURL("**/dashboard", new Page.WaitForURLOptions().setTimeout(60_000));

        boolean hasPlyUser = (Boolean) loginPage.evaluate("() => !!localStorage.getItem('ply_user')");
        assertTrue(hasPlyUser, "LocalStorage does not contain 'ply_user' after login.");

        // === 2️⃣  Зберігаємо стейт ===
        loginContext.storageState(new BrowserContext.StorageStateOptions().setPath(STORAGE_STATE));
        System.out.println("[INFO] Saved storage state to " + STORAGE_STATE.toAbsolutePath());

        loginContext.close();
    }

    //@BeforeEach
    void createContextFromStoredState() {
        // === 3️⃣  Створюємо контекст, використовуючи вже залогінений стейт ===
        context = browser.newContext(new Browser.NewContextOptions()
                .setIgnoreHTTPSErrors(true)
                .setBypassCSP(true)
                .setStorageStatePath(STORAGE_STATE)
        );
        page = context.newPage();

        // Health-check: відкриваємо Dashboard
        page.navigate(UI_BASE + "/dashboard",
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        System.out.println("[INFO] Opened dashboard with reused auth state");
    }

    //@AfterEach
    void closeContext() {
        if (context != null) context.close();
    }

    //@AfterAll
    void tearDown() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    // ===== Хелпери =====

    protected Page currentPage() { return page; }

    protected void openPath(String path) {
        page.navigate(UI_BASE + path,
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
    }
}
