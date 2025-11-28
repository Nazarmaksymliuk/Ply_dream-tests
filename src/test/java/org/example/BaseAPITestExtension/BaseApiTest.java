package org.example.BaseAPITestExtension;

import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseApiTest {

    protected static Playwright playwright;
    protected static APIRequestContext apiRequest;

    @BeforeAll
    static void setUpApiClient() {
        playwright = Playwright.create();

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "*/*");

        apiRequest = playwright.request().newContext(
                new APIRequest.NewContextOptions()
                        .setBaseURL("https://stage-api.getply.com") // TODO: заміни на свій бекенд
                        .setExtraHTTPHeaders(headers)
        );
    }

    @AfterAll
    static void tearDownApiClient() {
        if (apiRequest != null) {
            apiRequest.dispose();
        }
        if (playwright != null) {
            playwright.close();
        }
    }
}
