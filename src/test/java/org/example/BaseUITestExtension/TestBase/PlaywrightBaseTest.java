package org.example.BaseUITestExtension.TestBase;

import com.google.gson.Gson;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;
import org.example.config.TestEnvironment;
import org.example.creds.Users;
import org.example.routes.Routes;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Base class: logs in once and prepares shared BrowserContext + Page.
 * All tests inheriting this class work as an authenticated user.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class PlaywrightBaseTest {

    private static final Logger log = LoggerFactory.getLogger(PlaywrightBaseTest.class);

    protected Playwright playwright;
    protected APIRequestContext api;
    protected Browser browser;
    protected BrowserContext context;
    protected Page page;
    protected AuthResponse auth;
    protected UserDto currentUser;

    protected final Gson gson = new Gson();

    protected static class AuthResponse {
        String token;
        String refreshToken;
        String role;
        String expiresAt;
        int onboardingStep;
        boolean mfaRequired;
    }

    protected static class UserDto {
        String id;
        String email;
        String firstName;
        String lastName;
        String role;
        Boolean materialRequests;
        Boolean systemNotification;
        Boolean marketingUpdates;
        Boolean emailPurchaseOrderChatNotificationsEnabled;
    }

    @BeforeAll
    void beforeAllLoginAndBootUi() {
        playwright = Playwright.create();
        api = playwright.request().newContext(
                new APIRequest.NewContextOptions()
                        .setBaseURL(Routes.BASE_API_URL)
                        .setIgnoreHTTPSErrors(true)
        );

        APIResponse login = api.post(
                "/v3/login",
                RequestOptions.create().setData(Map.of(
                        "email", Users.ADMIN.email(),
                        "password", Users.ADMIN.password()
                ))
        );
        assertEquals(200, login.status(), "Login API failed: " + login.status() + " " + login.text());
        auth = gson.fromJson(login.text(), AuthResponse.class);
        assertNotNull(auth.token, "token is null");
        assertNotNull(auth.refreshToken, "refreshToken is null");

        APIResponse usersResp = api.get(
                "/users",
                RequestOptions.create().setHeader("Authorization", "Bearer " + auth.token)
        );
        assertEquals(200, usersResp.status(), "/users must be 200, got " + usersResp.status());
        UserDto[] users = gson.fromJson(usersResp.text(), UserDto[].class);
        assertTrue(users.length > 0, "Empty /users array");
        currentUser = users[0];

        String mfaLoginStateJson = buildMfaLoginState(auth);
        String plyUserJson = buildPlyUser(auth, currentUser);

        boolean headless = Boolean.parseBoolean(
                System.getProperty("headless",
                        System.getenv().getOrDefault("HEADLESS", "false"))
        );

        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(headless)
                        .setSlowMo(0)
                        .setArgs(Arrays.asList(
                                "--disk-cache-size=0",
                                "--disable-application-cache",
                                "--disable-dev-shm-usage"
                        ))
        );
        context = browser.newContext(
                new Browser.NewContextOptions()
                        .setIgnoreHTTPSErrors(true)
                        .setBypassCSP(true)
        );

        context.route("**/*", route -> {
            var req = route.request();
            var url = req.url();
            var headers = new HashMap<>(req.headers());

            headers.put("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.put("Pragma", "no-cache");
            headers.put("Expires", "0");

            if (url.contains("unleash-hosted.com")) {
                route.resume(new Route.ResumeOptions().setHeaders(headers));
                return;
            }
            if (url.startsWith(Routes.BASE_API_URL)) {
                headers.put("Authorization", "Bearer " + auth.token);
            }
            route.resume(new Route.ResumeOptions().setHeaders(headers));
        });

        String initScript = """
                (() => {
                  localStorage.setItem('ply_mfa_loginState', %s);
                  localStorage.setItem('ply_user', %s);
                })();
                """.formatted(mfaLoginStateJson, plyUserJson);
        context.addInitScript(initScript);

        page = context.newPage();

        context.storageState(new BrowserContext.StorageStateOptions()
                .setPath(Paths.get("build/auth-storage-state.json")));
    }

    @AfterAll
    void afterAllClose() {
        if (context != null) context.close();
        if (browser != null) browser.close();
        if (api != null) api.dispose();
        if (playwright != null) playwright.close();
    }

    protected Page newAuthenticatedPage() {
        return context.newPage();
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
        page.getByText(elementName).waitFor(
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

    protected String bearer() {
        return "Bearer " + auth.token;
    }

    private String buildMfaLoginState(AuthResponse a) {
        Map<String, Object> mfa = new LinkedHashMap<>();
        mfa.put("expiresAt", a.expiresAt != null ? a.expiresAt : "");
        mfa.put("mfaRequired", false);
        mfa.put("onboardingStep", a.onboardingStep);
        mfa.put("refreshToken", a.refreshToken);
        mfa.put("token", a.token);
        return gson.toJson(mfa);
    }

    private String buildPlyUser(AuthResponse a, UserDto u) {
        Map<String, Object> userState = new LinkedHashMap<>();
        userState.put("token", a.token);
        userState.put("expiresAt", a.expiresAt != null ? a.expiresAt : "");

        Map<String, Object> userObj = new LinkedHashMap<>();
        userObj.put("email", u.email);
        userObj.put("firstName", u.firstName);
        userObj.put("lastName", u.lastName);
        userObj.put("userId", u.id);
        userObj.put("roleName", u.role);
        userObj.put("isLogged", true);
        userObj.put("materialRequests", u.materialRequests);
        userObj.put("systemNotification", u.systemNotification);
        userObj.put("marketingUpdates", u.marketingUpdates);
        userObj.put("emailPurchaseOrderChatNotificationsEnabled", u.emailPurchaseOrderChatNotificationsEnabled);

        userState.put("user", userObj);

        Map<String, Object> integrations = new HashMap<>();
        integrations.put("plaid", "NOT_CONNECTED");
        integrations.put("railz", "NOT_CONNECTED");
        integrations.put("agave", "NOT_CONNECTED");
        integrations.put("integratedServiceType", null);
        integrations.put("lastSyncAt", null);
        userState.put("integrations", integrations);

        Map<String, Object> plyUser = new LinkedHashMap<>();
        plyUser.put("state", userState);
        plyUser.put("version", 1);

        return gson.toJson(plyUser);
    }
}
