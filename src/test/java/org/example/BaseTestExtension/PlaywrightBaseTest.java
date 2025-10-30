package org.example.BaseTestExtension;
import com.google.gson.Gson;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;

import java.nio.file.Paths;
import java.util.*;

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
                        .setIgnoreHTTPSErrors(true)

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
                        .setSlowMo(0)        // ← зручно локально, можна прибрати у CI
                        .setArgs(Arrays.asList(
                                "--disk-cache-size=0",
                                "--disable-application-cache",
                                "--disable-dev-shm-usage"
                        ))
        );
        context = browser.newContext(
                new Browser.NewContextOptions()
                        //.setExtraHTTPHeaders(Map.of("Authorization", "Bearer " + auth.token)
                        .setIgnoreHTTPSErrors(true)
                        .setBypassCSP(true) // іноді рятує, якщо SW/скрипт блокує

        );


        String bearer = "Bearer " + auth.token;

        // Додаємо no-cache до всіх запитів       //MAINNNNNNNNNN
        // Додаємо no-cache до всіх запитів       //MAINNNNNNNNNN
//        context.route("**/*", route -> {
//            Request req = route.request();
//            Map<String, String> headers = new HashMap<>(req.headers());
//            headers.put("Authorization", bearer);
//            headers.put("Cache-Control", "no-cache, no-store, must-revalidate");
//            headers.put("Pragma", "no-cache");
//            headers.put("Expires", "0");
//            route.resume(new Route.ResumeOptions().setHeaders(headers));
//        });

        context.route("**/*", route -> {   //SECOND MAINNNN
            var req = route.request();
            var url = req.url();
            var headers = new HashMap<>(req.headers());

            headers.put("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.put("Pragma", "no-cache");
            headers.put("Expires", "0");

            // НЕ чіпати Unleash
            if (url.contains("unleash-hosted.com")) {
                route.resume(new Route.ResumeOptions().setHeaders(headers));
                return;
            }
            // Для решти (або тільки вашого API) додати Bearer
            if (url.startsWith(API_BASE)) {
                headers.put("Authorization", "Bearer " + auth.token);
            }
            route.resume(new Route.ResumeOptions().setHeaders(headers));
        });


        // 6) Поставити ключі ДО старту сторінки      ///////////MAINNNNNNNNNN
//        String initScript = """
//                (() => {
//                  localStorage.setItem('ply_mfa_loginState', %s);
//                  localStorage.setItem('ply_user', %s);
//                })();
//                """.formatted(
//                gson.toJson(mfaLoginStateJson),
//                gson.toJson(plyUserJson)
//        );
//        context.addInitScript(initScript);

        String initScript = """
  (() => {
    localStorage.setItem('ply_mfa_loginState', %s);
    localStorage.setItem('ply_user', %s);
  })();
  """.formatted(
                mfaLoginStateJson,  // без gson.toJson(...)
                plyUserJson         // без gson.toJson(...)
        );
        context.addInitScript(initScript);


        // 7) Відкрити корінь — уже авторизовано
        page = context.newPage();

        //ЗАБРАВ, для того, щоб швидше піднімався тест і юзер зразу заходив на правильну сторінку
        //page.navigate(UI_BASE, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED).setTimeout(60000));

//        // 8) Переконатися, що бек прийняв токен (чекаємо /users == 200)
//        page.waitForResponse(r -> r.url().contains("/users") && r.status() == 200, () -> {
//        });
//        // (опційно) можна зачекати стабільний селектор
//        //page.waitForSelector("[class='header_dashboard_title']");
//
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
        page.navigate(
                UI_BASE + path,
                new Page.NavigateOptions()
                        .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                        .setTimeout(80000) // таймаут у мілісекундах (тут 60 секунд)
        );
    }


    //WAITS
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