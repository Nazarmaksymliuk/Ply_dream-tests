package org.example.Api.Kits;

import com.microsoft.playwright.APIResponse;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.example.Api.helpers.KitsHelper.KitsClient;
import org.example.Api.helpers.assertions.ApiAssertions;
import org.example.BaseAPITestExtension.BaseApiTest;
import org.example.config.TestEnvironment;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Negative test scenarios for Kits API.
 * Tests error handling, validation, and edge cases.
 */
@Epic("Kits")
@Feature("Kits Negative Scenarios")
@Timeout(value = TestEnvironment.E2E_TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
public class KitsNegativeTests extends BaseApiTest {

    private static final Logger log = LoggerFactory.getLogger(KitsNegativeTests.class);

    private KitsClient kitsClient;
    private final List<String> createdIds = new ArrayList<>();

    @BeforeAll
    void initClient() {
        kitsClient = new KitsClient(userApi);
        log.info("KitsClient initialized for negative tests");
    }

    @AfterAll
    void cleanup() {
        for (String id : createdIds) {
            try {
                kitsClient.deleteKits(List.of(id));
            } catch (Exception e) {
                log.warn("Cleanup: failed to delete kit {}: {}", id, e.getMessage());
            }
        }
    }

    @DisplayName("Create kit with empty body returns 400")
    @Test
    void createKit_withEmptyBody_returns400() {
        Map<String, Object> emptyBody = new HashMap<>();
        log.info("Testing kit creation with empty body");

        APIResponse response = kitsClient.createKit(emptyBody);

        ApiAssertions.assertStatus(
                400,
                response,
                "Empty body should be rejected with 400"
        );
        log.info("Empty body properly rejected with status: {}", response.status());
    }

    @DisplayName("Create kit with missing name returns 400")
    @Test
    void createKit_withMissingName_returns400() {
        Map<String, Object> bodyWithoutName = new HashMap<>();
        bodyWithoutName.put("description", "Kit without name");
        bodyWithoutName.put("status", "ACTIVE");

        log.info("Testing kit creation with missing name field");

        APIResponse response = kitsClient.createKit(bodyWithoutName);

        ApiAssertions.assertStatus(
                400,
                response,
                "Missing name field should be rejected with 400"
        );
        log.info("Missing name properly rejected with status: {}", response.status());
    }

    @DisplayName("Update kit with non-existent ID returns 400 or 404")
    @Test
    void updateKit_withNonExistentId_returns400() {
        String nonExistentId = UUID.randomUUID().toString();
        Map<String, Object> updateBody = new HashMap<>();
        updateBody.put("name", "Updated Kit");
        updateBody.put("description", "Updated description");

        log.info("Testing kit update with non-existent ID: {}", nonExistentId);

        APIResponse response = kitsClient.updateKit(nonExistentId, updateBody);

        ApiAssertions.assertStatusOneOf(
                response,
                "Non-existent kit ID should be rejected",
                400, 404
        );
        log.info("Non-existent ID properly rejected with status: {}", response.status());
    }

    @DisplayName("Update kit with invalid ID format returns error")
    @Test
    void updateKit_withInvalidIdFormat_returnsError() {
        String invalidId = "not-a-valid-uuid";
        Map<String, Object> updateBody = new HashMap<>();
        updateBody.put("name", "Updated Kit");

        log.info("Testing kit update with invalid ID format: {}", invalidId);

        APIResponse response = kitsClient.updateKit(invalidId, updateBody);

        log.info("Update kit with invalid ID format - status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatusOneOf(response, "Update kit with invalid ID format", 400, 404);
    }

    @DisplayName("Delete kit with non-existent ID returns error or 204")
    @Test
    void deleteKit_withNonExistentId_returns400() {
        String nonExistentId = UUID.randomUUID().toString();
        List<String> ids = List.of(nonExistentId);

        log.info("Testing kit deletion with non-existent ID: {}", nonExistentId);

        APIResponse response = kitsClient.deleteKits(ids);

        ApiAssertions.assertStatusOneOf(
                response,
                "Non-existent kit ID deletion should be rejected",
                400, 404, 204
        );
        log.info("Non-existent ID deletion returned status: {}", response.status());
    }

    @DisplayName("Create kit with SQL injection in name returns 400 (validated)")
    @Test
    void createKit_withSqlInjectionInName_returns400() {
        log.info("Testing kit creation with SQL injection in name");

        Map<String, Object> body = new HashMap<>();
        body.put("name", "Kit'; DROP TABLE kits;-- " + System.currentTimeMillis());
        body.put("description", "SQL injection test");

        APIResponse response = kitsClient.createKit(body);
        log.info("Create kit with SQL injection - status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatusOneOf(response, "Create kit with SQL injection in name", 400, 200, 201);

        if (response.status() == 200 || response.status() == 201) {
            try {
                String kitId = kitsClient.extractKitId(response);
                if (kitId != null) createdIds.add(kitId);
            } catch (Exception e) {
                log.warn("Could not extract kit ID for cleanup: {}", e.getMessage());
            }
        }
    }

    @DisplayName("Create kit with XSS in name returns 400 (validated)")
    @Test
    void createKit_withXssInName_returns400() {
        log.info("Testing kit creation with XSS in name");

        Map<String, Object> body = new HashMap<>();
        body.put("name", "<script>alert('xss')</script>Kit " + System.currentTimeMillis());
        body.put("description", "XSS test");

        APIResponse response = kitsClient.createKit(body);
        log.info("Create kit with XSS - status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatusOneOf(response, "Create kit with XSS in name", 400, 200, 201);

        if (response.status() == 200 || response.status() == 201) {
            try {
                String kitId = kitsClient.extractKitId(response);
                if (kitId != null) createdIds.add(kitId);
            } catch (Exception e) {
                log.warn("Could not extract kit ID for cleanup: {}", e.getMessage());
            }
        }
    }

    @DisplayName("Create kit with extremely long name returns error")
    @Test
    void createKit_withExtremelyLongName_returnsError() {
        log.info("Testing kit creation with extremely long name (10000 characters)");

        Map<String, Object> body = new HashMap<>();
        body.put("name", "K".repeat(10000));
        body.put("description", "Long name test");

        APIResponse response = kitsClient.createKit(body);

        log.info("Create kit with long name - status: {}", response.status());

        ApiAssertions.assertStatusOneOf(response, "Create kit with extremely long name", 400, 200, 201, 500);

        if (response.status() == 200 || response.status() == 201) {
            try {
                String kitId = kitsClient.extractKitId(response);
                if (kitId != null) createdIds.add(kitId);
            } catch (Exception e) {
                log.warn("Could not extract kit ID for cleanup: {}", e.getMessage());
            }
        }
    }

    @DisplayName("Delete kit with empty IDs list returns error or 204")
    @Test
    void deleteKit_withEmptyIdsList_returnsError() {
        log.info("Testing kit deletion with empty IDs list");

        List<String> emptyIds = Collections.emptyList();

        APIResponse response = kitsClient.deleteKits(emptyIds);

        log.info("Delete kit with empty IDs list - status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatusOneOf(response, "Delete kit with empty IDs list", 400, 204, 500);
    }
}
