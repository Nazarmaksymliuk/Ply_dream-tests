package org.example.Api.Kits;

import com.microsoft.playwright.APIResponse;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.example.Api.helpers.KitsHelper.KitsClient;
import org.example.Api.helpers.assertions.ApiAssertions;
import org.example.BaseAPITestExtension.BaseApiTest;
import org.example.config.TestEnvironment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

    @BeforeAll
    void initClient() {
        kitsClient = new KitsClient(userApi);
        log.info("KitsClient initialized for negative tests");
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

    @DisplayName("Update kit with non-existent ID returns 404")
    @Test
    void updateKit_withNonExistentId_returns404() {
        String nonExistentId = UUID.randomUUID().toString();
        Map<String, Object> updateBody = new HashMap<>();
        updateBody.put("name", "Updated Kit");
        updateBody.put("description", "Updated description");

        log.info("Testing kit update with non-existent ID: {}", nonExistentId);

        APIResponse response = kitsClient.updateKit(nonExistentId, updateBody);

        ApiAssertions.assertStatus(
                404,
                response,
                "Non-existent ID should return 404"
        );
        log.info("Non-existent ID properly rejected with status: {}", response.status());
    }

    @DisplayName("Delete kit with non-existent ID returns 404 or 204")
    @Test
    void deleteKit_withNonExistentId_returns404or204() {
        String nonExistentId = UUID.randomUUID().toString();
        List<String> ids = List.of(nonExistentId);

        log.info("Testing kit deletion with non-existent ID: {}", nonExistentId);

        APIResponse response = kitsClient.deleteKits(ids);

        ApiAssertions.assertStatusOneOf(
                response,
                "Non-existent ID deletion should return 404 or 204",
                404, 204
        );
        log.info("Non-existent ID deletion returned status: {}", response.status());
    }
}
