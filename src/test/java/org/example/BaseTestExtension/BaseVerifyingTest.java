package org.example.BaseTestExtension;

import com.google.gson.Gson;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.RequestOptions;
import com.microsoft.playwright.options.WaitUntilState;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class BaseVerifyingTest {
    // === URLs ===
    private static final String API_BASE = "https://stage-api.getply.com";
    private static final String LOGIN_PATH = "/v3/login";
    private static final String UI_BASE = "https://stage.getply.com";
    private static final String UI_DASHBOARD = UI_BASE + "/dashboard";

    // === Creds (краще через ENV у CI) ===
    private static final String EMAIL = System.getenv().getOrDefault("PLY_EMAIL", "maksimlukoleg56@gmail.com");
    private static final String PASSWORD = System.getenv().getOrDefault("PLY_PASSWORD", "Test+1234");

    private Playwright playwright;
    private APIRequestContext request;
    private final Gson gson = new Gson();

    // === DTOs під ваші відповіді ===
    static class AuthResponse {
        String token;
        String refreshToken;
        String role;
        String expiresAt;
        int onboardingStep;
        boolean mfaRequired;
    }
    static class UserDto {
        String id;
        String email;
        String firstName;
        String lastName;
        String role;
        Boolean materialRequests;
        Boolean systemNotification;
        Boolean marketingUpdates;
        Boolean emailPurchaseOrderChatNotificationsEnabled;
        // ...додай поля за потреби
    }

    @BeforeEach
    void setUp() {
        playwright = Playwright.create();
        request = playwright.request().newContext(
                new APIRequest.NewContextOptions().setBaseURL(API_BASE)
        );
    }

    @AfterEach
    void tearDown() {
        if (request != null) request.dispose();
        if (playwright != null) playwright.close();
    }

    @Test
    @DisplayName("Login via API → inject ply_mfa_loginState & ply_user → open Dashboard")
    void loginViaApi_andOpenDashboard() {
        // 1) Логін по API
        APIResponse login = request.post(
                LOGIN_PATH,
                RequestOptions.create().setData(Map.of("email", EMAIL, "password", PASSWORD))
        );
        assertEquals(200, login.status(), "Login API failed: " + login.status() + " " + login.text());
        AuthResponse auth = gson.fromJson(login.text(), AuthResponse.class);
        assertNotNull(auth.token, "token is null");
        assertNotNull(auth.refreshToken, "refreshToken is null");

        // 2) Підтягнути користувача з /users (щоб побудувати ply_user)
        APIResponse usersResp = request.get(
                "/users",
                RequestOptions.create().setHeader("Authorization", "Bearer " + auth.token)
        );
        assertEquals(200, usersResp.status(), "/users must be 200, got " + usersResp.status());
        UserDto[] users = gson.fromJson(usersResp.text(), UserDto[].class);
        assertTrue(users.length > 0, "Empty /users array");
        UserDto u = users[0];

        // 3) Побудувати значення localStorage:

        // 3a) ply_mfa_loginState — стан логіну (бачили на твоєму скріні)
        Map<String, Object> mfaLoginState = new LinkedHashMap<>();
        mfaLoginState.put("expiresAt", auth.expiresAt != null ? auth.expiresAt : "");
        mfaLoginState.put("mfaRequired", false);
        mfaLoginState.put("onboardingStep", auth.onboardingStep);
        mfaLoginState.put("refreshToken", auth.refreshToken);
        mfaLoginState.put("token", auth.token);

        // 3b) ply_user — користувацький стан (бачили на твоєму скріні)
        Map<String, Object> userState = new LinkedHashMap<>();
        userState.put("token", auth.token);                       // без "Bearer"
        userState.put("expiresAt", auth.expiresAt != null ? auth.expiresAt : "");

        Map<String, Object> userObj = new LinkedHashMap<>();
        userObj.put("email", u.email);
        userObj.put("firstName", u.firstName);
        userObj.put("lastName", u.lastName);
        userObj.put("userId", u.id);
        userObj.put("roleName", u.role);                          // "OWNER"
        userObj.put("isLogged", true);
        // корисні флаги (є в /users):
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

        // Значення у LS зберігаються як STRING, тому серіалізуємо в JSON-рядок:
        String mfaLoginStateJson = gson.toJson(mfaLoginState);
        String plyUserJson = gson.toJson(plyUser);

        // 4) Браузер і контекст (додаємо Authorization на всі XHR для надійності)
        Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(120)
        );
        BrowserContext context = browser.newContext(
                new Browser.NewContextOptions().setExtraHTTPHeaders(Map.of(
                        "Authorization", "Bearer " + auth.token
                ))
        );

        // 5) ВАЖЛИВО: кладемо обидва ключі ДО старту сторінки
        String initScript = """
      (() => {
        localStorage.setItem('ply_mfa_loginState', %s);
        localStorage.setItem('ply_user', %s);
      })();
      """.formatted(
                gson.toJson(mfaLoginStateJson), // gson.toJson(...) робить коректний JS-рядок
                gson.toJson(plyUserJson)
        );
        context.addInitScript(initScript);

        // 6) Відкрити дашборд (LS вже заповнений — UI стартує авторизованим)
        Page page = context.newPage();
        page.navigate(UI_DASHBOARD, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

        // 7) Переконатися, що бек прийняв токен (чекаємо /users == 200)
        Response okUsers = page.waitForResponse(
                r -> r.url().contains("/users") && r.status() == 200,
                () -> {} // Java-API вимагає Runnable
        );
        assertEquals(200, okUsers.status());

        // 8) Перевірка UI (постав свій стабільний селектор)
        page.waitForSelector("[class='header_dashboard_title']", new Page.WaitForSelectorOptions().setTimeout(20000));
        assertTrue(page.url().contains("/dashboard"), "Not on dashboard: " + page.url());

        // (опційно) зберегти авторизацію для наступних тестів
        context.storageState(new BrowserContext.StorageStateOptions()
                .setPath(Paths.get("build/auth-storage-state.json")));




        context.close();
        browser.close();
    }
}
