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
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
public abstract class BaseApiTest {

    private static final Logger log = LoggerFactory.getLogger(BaseApiTest.class);

    protected Playwright playwright;

    protected APIRequestContext userApi;
    protected APIRequestContext adminApi;

    private static volatile String cachedUserToken;
    private static volatile String cachedAdminToken;

    private static final String ADMIN_EMAIL =
            System.getenv().getOrDefault("PLY_ADMIN_EMAIL", "admin@getply.com");
    private static final String ADMIN_PASSWORD =
            System.getenv().getOrDefault("PLY_ADMIN_PASSWORD", "WJoXYjE1n8m8!J");

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
        cachedUserToken = userToken;

        String adminToken = getOrCreateToken(
                ADMIN_EMAIL, ADMIN_PASSWORD, "admin", defaultHeaders
        );
        cachedAdminToken = adminToken;

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

        String cached = label.equals("admin") ? cachedAdminToken : cachedUserToken;
        if (cached != null) return cached;

        synchronized (BaseApiTest.class) {
            cached = label.equals("admin") ? cachedAdminToken : cachedUserToken;
            if (cached != null) return cached;

            Logger logger = LoggerFactory.getLogger(BaseApiTest.class);
            logger.info("Login ({}) starting...", label.toUpperCase());

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

                logger.info("Login ({}) status: {}", label, status);
                logger.debug("Login ({}) body: {}", label, text);

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

                if (label.equals("admin")) cachedAdminToken = token;
                else cachedUserToken = token;

                return token;
            }
        }
    }
}
