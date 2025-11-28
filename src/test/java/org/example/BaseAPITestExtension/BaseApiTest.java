package org.example.BaseAPITestExtension;

import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import org.example.Api.helpers.LoginHelper.LoginClient;
import org.example.Api.helpers.LoginHelper.LoginResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
public abstract class BaseApiTest {

    protected Playwright playwright;
    protected APIRequestContext apiRequest;

    // 🔒 спільний токен для ВСІХ тест-класів
    private static volatile String cachedToken;

    @BeforeAll
    void setUpApiClient() throws IOException {
        playwright = Playwright.create();

        Map<String, String> baseHeaders = new HashMap<>();
        baseHeaders.put("Content-Type", "application/json");
        baseHeaders.put("Accept", "*/*");

        // 1️⃣ беремо токен (або логінимось один раз)
        String token = getOrCreateToken(baseHeaders);

        // 2️⃣ створюємо контекст з Bearer токеном ДЛЯ ЦЬОГО класу
        Map<String, String> authHeaders = new HashMap<>(baseHeaders);
        authHeaders.put("Authorization", "Bearer " + token);

        apiRequest = playwright.request().newContext(
                new APIRequest.NewContextOptions()
                        .setBaseURL("https://stage-api.getply.com")
                        .setExtraHTTPHeaders(authHeaders)
        );
    }

    @AfterAll
    void tearDownApiClient() {
        if (apiRequest != null) {
            apiRequest.dispose();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    // ---------- helpers ----------

    private static String getOrCreateToken(Map<String, String> baseHeaders) throws IOException {
        if (cachedToken != null) {
            return cachedToken;
        }

        synchronized (BaseApiTest.class) {
            if (cachedToken != null) {
                return cachedToken;
            }

            // окремий Playwright тільки для логіну
            try (Playwright pw = Playwright.create()) {
                APIRequestContext loginContext = pw.request().newContext(
                        new APIRequest.NewContextOptions()
                                .setBaseURL("https://stage-api.getply.com")
                                .setExtraHTTPHeaders(baseHeaders)
                );

                LoginClient loginClient = new LoginClient(loginContext);

                APIResponse loginResponse = loginClient.login(
                        "maksimlukoleg56@gmail.com",
                        "Test+1234"
                );

                int status = loginResponse.status();
                String bodyText = loginResponse.text();
                System.out.println("LOGIN status: " + status);
                System.out.println("LOGIN body: " + bodyText);

                String token;
                if (status == 200 || status == 201) {
                    LoginResponse loginBody = loginClient.parseLoginResponse(loginResponse);
                    token = loginBody.getToken();
                    if (token == null || token.isEmpty()) {
                        throw new IllegalStateException("Login succeeded but token is null or empty");
                    }
                } else if (status == 409 && bodyText.contains("uq_refresh_token")) {
                    // ⚠️ костиль на бекенд: токен вже є в БД – дістаємо його з повідомлення
                    token = extractTokenFromConflict(bodyText);
                    if (token == null || token.isEmpty()) {
                        throw new IllegalStateException(
                                "Login returned 409 with refresh token conflict, but token could not be extracted"
                        );
                    }
                } else {
                    throw new IllegalStateException(
                            "Login failed. Expected 200/201 but got: " + status +
                                    ". Body: " + bodyText
                    );
                }

                loginContext.dispose();
                cachedToken = token; // зберігаємо для всіх наступних тестів
                return cachedToken;
            }
        }
    }

    private static String extractTokenFromConflict(String body) {
        // шукаємо шматок (token)=(JWT...)
        Pattern p = Pattern.compile("Key \\(token\\)=\\(([^)]+)\\)");
        Matcher m = p.matcher(body);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }
}
