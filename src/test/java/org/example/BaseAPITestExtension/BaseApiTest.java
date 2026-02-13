package org.example.BaseAPITestExtension;

import com.microsoft.playwright.*;
import org.example.Api.helpers.LoginHelper.LoginClient;
import org.example.Api.helpers.LoginHelper.LoginResponse;
import org.example.creds.Users;
import org.example.routes.Routes;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
public abstract class BaseApiTest {

    private static final Logger log = LoggerFactory.getLogger(BaseApiTest.class);

    protected Playwright playwright;

    protected APIRequestContext userApi;
    protected APIRequestContext adminApi;

    private static volatile CachedToken cachedUserToken;
    private static volatile CachedToken cachedAdminToken;

    @BeforeAll
    void setUp() throws IOException {
        playwright = Playwright.create();

        Map<String, String> defaultHeaders = Map.of(
                "Content-Type", "application/json",
                "Accept", "*/*"
        );

        String userToken = getOrCreateToken(
                Users.ADMIN.email(), Users.ADMIN.password(), "user", defaultHeaders
        );

        String adminToken = getOrCreateToken(
                Users.SUPER_ADMIN.email(), Users.SUPER_ADMIN.password(), "admin", defaultHeaders
        );

        userApi = createApiContext(userToken);
        adminApi = createApiContext(adminToken);

        log.info("User API context ready");
        log.info("Admin API context ready");
    }

    @AfterAll
    void tearDown() {
        if (userApi != null) userApi.dispose();
        if (adminApi != null) adminApi.dispose();
        if (playwright != null) playwright.close();
    }

    private APIRequestContext createApiContext(String token) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "*/*");
        headers.put("Authorization", "Bearer " + token);

        return playwright.request().newContext(
                new APIRequest.NewContextOptions()
                        .setBaseURL(Routes.BASE_API_URL)
                        .setExtraHTTPHeaders(headers)
        );
    }

    private static String getOrCreateToken(
            String email,
            String password,
            String label,
            Map<String, String> headers
    ) throws IOException {

        CachedToken cached = "admin".equals(label) ? cachedAdminToken : cachedUserToken;
        if (cached != null && !cached.isExpired()) {
            return cached.token;
        }

        synchronized (BaseApiTest.class) {
            cached = "admin".equals(label) ? cachedAdminToken : cachedUserToken;
            if (cached != null && !cached.isExpired()) {
                return cached.token;
            }

            log.info("Login ({}) starting...", label.toUpperCase());

            try (Playwright pw = Playwright.create()) {
                APIRequestContext loginContext = pw.request().newContext(
                        new APIRequest.NewContextOptions()
                                .setBaseURL(Routes.BASE_API_URL)
                                .setExtraHTTPHeaders(headers)
                );

                LoginClient loginClient = new LoginClient(loginContext);
                APIResponse loginResponse = loginClient.login(email, password);

                int status = loginResponse.status();
                String text = loginResponse.text();

                log.info("Login ({}) status: {}", label, status);
                log.debug("Login ({}) body: {}", label, text);

                if (status != 200 && status != 201) {
                    throw new IllegalStateException(
                            "Login as " + label + " failed: status=" + status + ", body=" + text
                    );
                }

                LoginResponse parsed = loginClient.parseLoginResponse(loginResponse);
                String token = parsed.getToken();

                if (token == null || token.isEmpty()) {
                    throw new IllegalStateException("Token (" + label + ") is null or empty");
                }

                Instant expiresAt = parseExpiresAt(parsed.getExpiresAt());
                CachedToken newCached = new CachedToken(token, expiresAt);

                if ("admin".equals(label)) cachedAdminToken = newCached;
                else cachedUserToken = newCached;

                return token;
            }
        }
    }

    private static Instant parseExpiresAt(String expiresAt) {
        if (expiresAt == null || expiresAt.isEmpty()) {
            // Default: assume token valid for 55 minutes (safety margin under 1h)
            return Instant.now().plusSeconds(55 * 60);
        }
        try {
            return Instant.parse(expiresAt);
        } catch (Exception e) {
            log.warn("Could not parse expiresAt '{}', defaulting to 55min from now", expiresAt);
            return Instant.now().plusSeconds(55 * 60);
        }
    }

    private static class CachedToken {
        final String token;
        final Instant expiresAt;

        CachedToken(String token, Instant expiresAt) {
            this.token = token;
            this.expiresAt = expiresAt;
        }

        boolean isExpired() {
            // Consider expired 2 minutes before actual expiry to avoid race conditions
            return Instant.now().isAfter(expiresAt.minusSeconds(120));
        }
    }
}
