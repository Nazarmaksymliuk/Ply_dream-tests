package org.example.BaseAPITestExtension;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.RequestOptions;
import org.example.Api.helpers.LoginHelper.LoginClient;
import org.example.Api.helpers.LoginHelper.LoginResponse;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
public abstract class BaseApiTest {

    protected Playwright playwright;

    // 🔹 Контексти для запитів
    protected APIRequestContext userApi;
    protected APIRequestContext adminApi;

    // 🔹 Статичні кешовані токени (спільні для всіх класів)
    private static volatile String cachedUserToken;
    private static volatile String cachedAdminToken;

    // 🔹 Тести-юзери
    private static final String USER_EMAIL = "maksimlukoleg56@gmail.com";
    private static final String USER_PASSWORD = "Test+1234";

    // 🔹 Адмінські креденшали
    private static final String ADMIN_EMAIL = "admin@getply.com";  // <- впиши свого
    private static final String ADMIN_PASSWORD = "WJoXYjE1n8m8!J";     // <- впиши свого

    @BeforeAll
    void setUp() throws IOException {
        playwright = Playwright.create();

        Map<String, String> defaultHeaders = Map.of(
                "Content-Type", "application/json",
                "Accept", "*/*"
        );

        // 1️⃣ Логін юзера
        String userToken = getOrCreateToken(
                USER_EMAIL,
                USER_PASSWORD,
                "user",
                defaultHeaders
        );
        cachedUserToken = userToken;

        // 2️⃣ Логін адміна
        String adminToken = getOrCreateToken(
                ADMIN_EMAIL,
                ADMIN_PASSWORD,
                "admin",
                defaultHeaders
        );
        cachedAdminToken = adminToken;

        // 3️⃣ Створюємо окремі API контексти
        userApi = createApiContext(userToken);
        adminApi = createApiContext(adminToken);

        System.out.println("=== USER CONTEXT READY ===");
        System.out.println("=== ADMIN CONTEXT READY ===");
    }

    @AfterAll
    void tearDown() {
        if (userApi != null) userApi.dispose();
        if (adminApi != null) adminApi.dispose();
        if (playwright != null) playwright.close();
    }

    // -----------------------------------------------------------
    //                 INTERNAL HELPERS
    // -----------------------------------------------------------

    private APIRequestContext createApiContext(String token) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "*/*");
        headers.put("Authorization", "Bearer " + token);

        return playwright.request().newContext(
                new APIRequest.NewContextOptions()
                        .setBaseURL("https://stage-api.getply.com")
                        .setExtraHTTPHeaders(headers)
        );
    }

    /**
     * Generic login method for both user and admin tokens.
     */
    private static String getOrCreateToken(
            String email,
            String password,
            String label,
            Map<String, String> headers
    ) throws IOException {

        String cached = label.equals("admin") ? cachedAdminToken : cachedUserToken;
        if (cached != null) return cached;

        synchronized (BaseApiTest.class) {

            cached = label.equals("admin") ? cachedAdminToken : cachedUserToken;
            if (cached != null) return cached;

            System.out.println("=== LOGIN (" + label.toUpperCase() + ") START ===");

            try (Playwright pw = Playwright.create()) {

                APIRequestContext loginContext = pw.request().newContext(
                        new APIRequest.NewContextOptions()
                                .setBaseURL("https://stage-api.getply.com")
                                .setExtraHTTPHeaders(headers)
                );

                LoginClient loginClient = new LoginClient(loginContext);
                APIResponse loginResponse = loginClient.login(email, password);

                int status = loginResponse.status();
                String text = loginResponse.text();

                System.out.println("LOGIN (" + label + ") STATUS: " + status);
                System.out.println("LOGIN (" + label + ") BODY: " + text);

                String token;

                if (status == 200 || status == 201) {
                    LoginResponse parsed = loginClient.parseLoginResponse(loginResponse);
                    token = parsed.getToken();
                } else if (status == 409 && text.contains("uq_refresh_token")) {
                    token = extractTokenFromConflict(text);
                } else {
                    throw new IllegalStateException(
                            "Login as " + label + " failed: status=" + status + ", body=" + text
                    );
                }

                if (token == null || token.isEmpty()) {
                    throw new IllegalStateException("Token (" + label + ") is null or empty");
                }

                if (label.equals("admin")) cachedAdminToken = token;
                else cachedUserToken = token;

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
