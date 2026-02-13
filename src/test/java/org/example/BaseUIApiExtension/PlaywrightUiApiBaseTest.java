package org.example.BaseUIApiExtension;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;
import org.example.Api.helpers.LoginHelper.LoginClient;
import org.example.Api.helpers.LoginHelper.LoginResponse;
import org.example.BaseAPITestExtension.BaseApiTest;
import org.example.UI.PageObjectModels.Authorization.SignIn.SignInPage;
import org.example.config.TestEnvironment;
import org.example.creds.Users;
import org.example.routes.Routes;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class PlaywrightUiApiBaseTest extends BaseApiTest {

    private static final Logger log = LoggerFactory.getLogger(PlaywrightUiApiBaseTest.class);

    protected static final String DASHBOARD_PATH = "/dashboard";

    private static final String ADMIN_EMAIL =
            System.getenv().getOrDefault("PLY_ADMIN_EMAIL", "admin@getply.com");
    private static final String ADMIN_PASSWORD =
            System.getenv().getOrDefault("PLY_ADMIN_PASSWORD", "WJoXYjE1n8m8!J");

    protected APIRequestContext adminApi;
    private static volatile String cachedAdminToken;

    protected Playwright playwright;
    protected Browser browser;
    protected BrowserContext context;
    protected Page page;

    protected APIRequestContext userApi;
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

        String userToken = getOrCreateUserToken(Users.ADMIN.email(), Users.ADMIN.password());
        userApi = createApiContextWithBearer(userToken);

        String adminToken = getOrCreateToken("admin", ADMIN_EMAIL, ADMIN_PASSWORD);
        adminApi = createApiContextWithBearer(adminToken);

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
    void afterAll_closeEverything() {
        if (userApi != null) userApi.dispose();
        if (adminApi != null) adminApi.dispose();
        if (context != null) context.close();
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
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

    private APIRequestContext createApiContextWithBearer(String jwt) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "*/*");
        headers.put("Authorization", "Bearer " + jwt);

        return playwright.request().newContext(
                new APIRequest.NewContextOptions()
                        .setBaseURL(Routes.BASE_API_URL)
                        .setExtraHTTPHeaders(headers)
                        .setIgnoreHTTPSErrors(true)
        );
    }

    private static String getOrCreateUserToken(String email, String password) throws IOException {
        if (cachedUserToken != null) return cachedUserToken;

        synchronized (PlaywrightUiApiBaseTest.class) {
            if (cachedUserToken != null) return cachedUserToken;

            log.info("Login (USER API) starting...");

            try (Playwright pw = Playwright.create()) {
                APIRequestContext loginContext = pw.request().newContext(
                        new APIRequest.NewContextOptions()
                                .setBaseURL(Routes.BASE_API_URL)
                                .setExtraHTTPHeaders(Map.of(
                                        "Content-Type", "application/json",
                                        "Accept", "*/*"
                                ))
                                .setIgnoreHTTPSErrors(true)
                );

                LoginClient loginClient = new LoginClient(loginContext);
                APIResponse loginResponse = loginClient.login(email, password);

                int status = loginResponse.status();
                log.info("Login (user) status: {}", status);

                if (status != 200 && status != 201) {
                    throw new IllegalStateException("User API login failed: status=" + status + ", body=" + loginResponse.text());
                }

                LoginResponse parsed = loginClient.parseLoginResponse(loginResponse);
                String token = parsed.getToken();

                if (token == null || token.isBlank()) {
                    throw new IllegalStateException("User token is null/empty after API login");
                }

                cachedUserToken = token;
                return token;
            }
        }
    }

    private static String getOrCreateToken(String label, String email, String password) throws IOException {
        if ("admin".equals(label) && cachedAdminToken != null) return cachedAdminToken;
        if ("user".equals(label) && cachedUserToken != null) return cachedUserToken;

        synchronized (PlaywrightUiApiBaseTest.class) {
            if ("admin".equals(label) && cachedAdminToken != null) return cachedAdminToken;
            if ("user".equals(label) && cachedUserToken != null) return cachedUserToken;

            log.info("Login ({}) starting...", label.toUpperCase());

            try (Playwright pw = Playwright.create()) {
                APIRequestContext loginContext = pw.request().newContext(
                        new APIRequest.NewContextOptions()
                                .setBaseURL(Routes.BASE_API_URL)
                                .setExtraHTTPHeaders(Map.of(
                                        "Content-Type", "application/json",
                                        "Accept", "*/*"
                                ))
                                .setIgnoreHTTPSErrors(true)
                );

                LoginClient loginClient = new LoginClient(loginContext);
                APIResponse loginResponse = loginClient.login(email, password);

                int status = loginResponse.status();
                log.info("Login ({}) status: {}", label, status);

                if (status != 200 && status != 201) {
                    throw new IllegalStateException(label + " API login failed: status=" + status + ", body=" + loginResponse.text());
                }

                LoginResponse parsed = loginClient.parseLoginResponse(loginResponse);
                String token = parsed.getToken();

                if (token == null || token.isBlank()) {
                    throw new IllegalStateException(label + " token is null/empty");
                }

                if ("admin".equals(label)) cachedAdminToken = token;
                else cachedUserToken = token;

                return token;
            }
        }
    }
}
