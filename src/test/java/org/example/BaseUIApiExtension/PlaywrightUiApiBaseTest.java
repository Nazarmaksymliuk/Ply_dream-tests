package org.example.BaseUIApiExtension;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;
import org.example.Api.helpers.LoginHelper.LoginClient;
import org.example.Api.helpers.LoginHelper.LoginResponse;
import org.example.UI.PageObjectModels.Authorization.SignIn.SignInPage;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class PlaywrightUiApiBaseTest {

    // =======================
    // URLs
    // =======================
    protected static final String UI_BASE  = "https://dev.getply.com";
    protected static final String API_BASE = "https://dev-api.getply.com";
    protected static final String DASHBOARD_PATH = "/dashboard";

    // =======================
    // Creds (same user for UI + API)
    // =======================
    protected static final String USER_EMAIL =
            System.getenv().getOrDefault("PLY_EMAIL", "maksimlukoleg56@gmail.com");
    protected static final String USER_PASSWORD =
            System.getenv().getOrDefault("PLY_PASSWORD", "Test+1234");

    // =======================
    // Playwright UI objects
    // =======================
    protected Playwright playwright;
    protected Browser browser;
    protected BrowserContext context;
    protected Page page;

    // =======================
    // API context (user)
    // =======================
    protected APIRequestContext userApi;

    // =======================
    // Cached token (for whole JVM run)
    // =======================
    private static volatile String cachedUserToken;

    @BeforeAll
    void beforeAll_setupUiAndApi() throws IOException {
        playwright = Playwright.create();

        boolean headless = Boolean.parseBoolean(
                System.getProperty("headless",
                        System.getenv().getOrDefault("HEADLESS", "false"))
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

        // =======================
        // 1) API LOGIN (user) -> userApi
        // =======================
        String userToken = getOrCreateUserToken(USER_EMAIL, USER_PASSWORD);
        userApi = createApiContextWithBearer(userToken);

        // =======================
        // 2) UI LOGIN (same user)
        // =======================
        context = browser.newContext(new Browser.NewContextOptions()
                .setIgnoreHTTPSErrors(true)
                .setBypassCSP(true)
        );

        context.setDefaultTimeout(50_000);
        context.setDefaultNavigationTimeout(50_000);

        page = context.newPage();

        // optional diagnostics
        page.onResponse(r -> { if (r.status() >= 400) System.out.println("[HTTP " + r.status() + "] " + r.url()); });
        page.onConsoleMessage(m -> System.out.println("[CONSOLE] " + m.type() + " " + m.text()));

        page.navigate(UI_BASE, new Page.NavigateOptions().setWaitUntil(WaitUntilState.COMMIT));

        SignInPage signIn = new SignInPage(page);
        page.waitForResponse(
                r -> r.url().contains("/users") && r.status() == 200,
                () -> signIn.signIntoApplication(USER_EMAIL, USER_PASSWORD)
        );

        // sanity: we are not on sign-in
        page.waitForURL(u -> !u.toString().contains("/sign-in"),
                new Page.WaitForURLOptions().setTimeout(60_000));

//        // optional: go to dashboard
//        page.navigate(UI_BASE + DASHBOARD_PATH, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
//        assertFalse(page.url().contains("/sign-in"), "UI login failed, still on /sign-in");
    }

    @AfterAll
    void afterAll_closeEverything() {
        if (userApi != null) userApi.dispose();

        if (context != null) context.close();
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    // =======================
    // UI helper
    // =======================
    protected void openPath(String path) {
        page.navigate(
                UI_BASE + path,
                new Page.NavigateOptions()
                        .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                        .setTimeout(100_000)
        );
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

    // =======================
    // API helpers
    // =======================
    private APIRequestContext createApiContextWithBearer(String jwt) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "*/*");
        headers.put("Authorization", "Bearer " + jwt);

        return playwright.request().newContext(
                new APIRequest.NewContextOptions()
                        .setBaseURL(API_BASE)
                        .setExtraHTTPHeaders(headers)
                        .setIgnoreHTTPSErrors(true)
        );
    }

    private static String getOrCreateUserToken(String email, String password) throws IOException {
        if (cachedUserToken != null) return cachedUserToken;

        synchronized (PlaywrightUiApiBaseTest.class) {
            if (cachedUserToken != null) return cachedUserToken;

            Map<String, String> headers = Map.of(
                    "Content-Type", "application/json",
                    "Accept", "*/*"
            );

            System.out.println("=== LOGIN (USER API) START ===");

            try (Playwright pw = Playwright.create()) {
                APIRequestContext loginContext = pw.request().newContext(
                        new APIRequest.NewContextOptions()
                                .setBaseURL(API_BASE)
                                .setExtraHTTPHeaders(headers)
                                .setIgnoreHTTPSErrors(true)
                );

                LoginClient loginClient = new LoginClient(loginContext);
                APIResponse loginResponse = loginClient.login(email, password);

                int status = loginResponse.status();
                String body = loginResponse.text();

                System.out.println("LOGIN (user) STATUS: " + status);
                // System.out.println("LOGIN (user) BODY: " + body); // не друкуй токен

                String token;
                if (status == 200 || status == 201) {
                    LoginResponse parsed = loginClient.parseLoginResponse(loginResponse);
                    token = parsed.getToken();
                } else if (status == 409 && body.contains("uq_refresh_token")) {
                    token = extractTokenFromConflict(body);
                } else {
                    throw new IllegalStateException("User API login failed: status=" + status + ", body=" + body);
                }

                if (token == null || token.isBlank()) {
                    throw new IllegalStateException("User token is null/empty after API login");
                }

                cachedUserToken = token;
                return token;
            }
        }
    }

    private static String extractTokenFromConflict(String body) {
        Pattern p = Pattern.compile("Key \\(token\\)=\\(([^)]+)\\)");
        Matcher m = p.matcher(body);
        return m.find() ? m.group(1) : null;
    }
}
