package org.example.BaseTestExtension;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


/**
 * Базовий клас: один раз логіниться та готує спільний BrowserContext + Page.
 * Усі тести, що наслідують цей клас, одразу працюють як залогінений користувач.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class PlaywrightBaseTest {
    // === URLs ===
    protected static final String API_BASE = "https://stage-api.getply.com";
    protected static final String LOGIN_PATH = "/v3/login";
    protected static final String UI_BASE = "https://stage.getply.com";
    protected static final String UI_DASHBOARD = UI_BASE + "/dashboard";

    // === Creds (краще передавати через ENV у CI) ===
    protected static final String EMAIL = System.getenv().getOrDefault("PLY_EMAIL", "maksimlukoleg56@gmail.com");
    protected static final String PASSWORD = System.getenv().getOrDefault("PLY_PASSWORD", "Test+1234");

    // === Shared objects for all tests ===
    protected Playwright playwright;
    protected APIRequestContext api;
    protected Browser browser;
    protected BrowserContext context;
    protected Page page;                // головна сторінка, уже залогінена
    protected AuthResponse auth;        // дані логіну
    protected UserDto currentUser;      // поточний користувач з /users

    protected final Gson gson = new Gson();

    // === DTOs під ваші відповіді ===
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
        // 1) Playwright + API контекст
        playwright = Playwright.create();
        api = playwright.request().newContext(
                new APIRequest.NewContextOptions().setBaseURL(API_BASE)
        );

        // 2) Логін
        APIResponse login = api.post(
                LOGIN_PATH,
                RequestOptions.create().setData(Map.of(
                        "email", EMAIL,
                        "password", PASSWORD
                ))
        );
        assertEquals(200, login.status(), "Login API failed: " + login.status() + " " + login.text());
        auth = gson.fromJson(login.text(), AuthResponse.class);
        assertNotNull(auth.token, "token is null");
        assertNotNull(auth.refreshToken, "refreshToken is null");

        // 3) /users для побудови ply_user
        APIResponse usersResp = api.get(
                "/users",
                RequestOptions.create().setHeader("Authorization", "Bearer " + auth.token)
        );
        assertEquals(200, usersResp.status(), "/users must be 200, got " + usersResp.status());
        UserDto[] users = gson.fromJson(usersResp.text(), UserDto[].class);
        assertTrue(users.length > 0, "Empty /users array");
        currentUser = users[0];

        // 4) Підготувати значення localStorage (ply_mfa_loginState + ply_user)
        String mfaLoginStateJson = buildMfaLoginState(auth);
        String plyUserJson = buildPlyUser(auth, currentUser);

        // 5) Запустити браузер і створити спільний контекст
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(false)   // ← зроби true у CI
                        .setSlowMo(80)        // ← зручно локально, можна прибрати у CI
        );
        context = browser.newContext(
                new Browser.NewContextOptions()
                        //.setExtraHTTPHeaders(Map.of("Authorization", "Bearer " + auth.token)
                        .setIgnoreHTTPSErrors(true)
        );

        // 6) Поставити ключі ДО старту сторінки
        String initScript = """
                (() => {
                  localStorage.setItem('ply_mfa_loginState', %s);
                  localStorage.setItem('ply_user', %s);
                })();
                """.formatted(
                gson.toJson(mfaLoginStateJson),
                gson.toJson(plyUserJson)
        );
        context.addInitScript(initScript);

//        // 7) Відкрити корінь — уже авторизовано
        page = context.newPage();
        page.navigate(UI_BASE, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

        // 8) Переконатися, що бек прийняв токен (чекаємо /users == 200)
        page.waitForResponse(r -> r.url().contains("/users") && r.status() == 200, () -> {
        });
        // (опційно) можна зачекати стабільний селектор
        //page.waitForSelector("[class='header_dashboard_title']");

        // 9) (опційно) зберегти state для перезапусків/паралелі
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

    // === ХЕЛПЕРИ ДЛЯ ДОЧІРНІХ ТЕСТІВ ===

    /**
     * Створює нову вкладку у вже авторизованому контексті.
     */
    protected Page newAuthenticatedPage() {
        return context.newPage();
    }

    /**
     * Відкрити шлях у поточній сторінці (наприклад, "/invoices").
     */
    protected void openPath(String path) {
        page.navigate(UI_BASE + path, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
    }

    /**
     * Доступ до токена, якщо потрібен у тесті.
     */
    protected String bearer() {
        return "Bearer " + auth.token;
    }

    // === Побудова LS-значень ===
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
        userState.put("token", a.token);                                   // без "Bearer"
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