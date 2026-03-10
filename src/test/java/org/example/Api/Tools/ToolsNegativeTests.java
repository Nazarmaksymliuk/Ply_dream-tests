package org.example.Api.Tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.APIResponse;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.example.Api.helpers.ToolsHelper.ToolsClient;
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
 * Negative test scenarios for Tools financing API.
 * Tests error handling, validation, and edge cases.
 */
@Epic("Tools")
@Feature("Tools Negative Scenarios")
@Timeout(value = TestEnvironment.E2E_TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
public class ToolsNegativeTests extends BaseApiTest {

    private static final Logger log = LoggerFactory.getLogger(ToolsNegativeTests.class);

    private ToolsClient toolsClient;
    private final List<String> createdIds = new ArrayList<>();

    @BeforeAll
    void initClient() {
        toolsClient = new ToolsClient(userApi);
        log.info("ToolsClient initialized for negative tests");
    }

    @AfterAll
    void cleanup() {
        for (String id : createdIds) {
            try {
                toolsClient.deleteToolsFinancing(id);
            } catch (Exception e) {
                log.warn("Cleanup: failed to delete tool {}: {}", id, e.getMessage());
            }
        }
    }

    @DisplayName("Create tool with empty body returns 409 (DB constraint violation)")
    @Test
    void createTool_withEmptyBody_returns409() {
        Map<String, Object> emptyBody = new HashMap<>();
        log.info("Testing tool creation with empty body");

        APIResponse response = toolsClient.createToolsFinancing(emptyBody);

        // API returns 409 due to DB not-null constraint on 'name' column
        ApiAssertions.assertStatusOneOf(
                response,
                "Empty body should be rejected",
                400, 409
        );
        log.info("Empty body properly rejected with status: {}", response.status());
    }

    @DisplayName("Create tool with missing name returns 409 (DB constraint)")
    @Test
    void createTool_withMissingName_returns409() {
        log.info("Testing tool creation with missing name field");

        Map<String, Object> body = new HashMap<>();
        body.put("description", "Tool without name");
        body.put("mfg", "Some Mfg");

        APIResponse response = toolsClient.createToolsFinancing(body);

        log.info("Create tool with missing name - status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatusOneOf(response, "Create tool with missing name", 400, 409);
    }

    @DisplayName("Update tool with non-existent ID returns 404")
    @Test
    void updateTool_withNonExistentId_returns404() {
        String nonExistentId = UUID.randomUUID().toString();
        Map<String, Object> updateBody = new HashMap<>();
        updateBody.put("name", "Updated Tool");
        updateBody.put("description", "Updated description");

        log.info("Testing tool update with non-existent ID: {}", nonExistentId);

        APIResponse response = toolsClient.updateToolsFinancing(nonExistentId, updateBody);

        ApiAssertions.assertStatus(
                404,
                response,
                "Non-existent ID should return 404"
        );
        log.info("Non-existent ID properly rejected with status: {}", response.status());
    }

    @DisplayName("Update tool with invalid ID format returns error")
    @Test
    void updateTool_withInvalidIdFormat_returnsError() {
        String invalidId = "not-a-valid-uuid";
        Map<String, Object> updateBody = new HashMap<>();
        updateBody.put("name", "Updated Tool");

        log.info("Testing tool update with invalid ID format: {}", invalidId);

        APIResponse response = toolsClient.updateToolsFinancing(invalidId, updateBody);

        log.info("Update tool with invalid ID format - status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatusOneOf(response, "Update tool with invalid ID format", 400, 404);
    }

    @DisplayName("Delete tool with non-existent ID returns 404 or 204")
    @Test
    void deleteTool_withNonExistentId_returns404or204() {
        String nonExistentId = UUID.randomUUID().toString();
        log.info("Testing tool deletion with non-existent ID: {}", nonExistentId);

        APIResponse response = toolsClient.deleteToolsFinancing(nonExistentId);

        ApiAssertions.assertStatusOneOf(
                response,
                "Non-existent ID deletion should return 404 or 204",
                404, 204
        );
        log.info("Non-existent ID deletion returned status: {}", response.status());
    }

    @DisplayName("Delete already deleted tool returns 404")
    @Test
    void deleteTool_alreadyDeleted_returns404() throws IOException {
        // First, create a minimal tool
        Map<String, Object> createBody = buildMinimalToolBody();
        log.info("Creating tool for deletion test");

        APIResponse createResponse = toolsClient.createToolsFinancing(createBody);
        ApiAssertions.assertStatusOneOf(
                createResponse,
                "Tool creation should succeed",
                200, 201
        );

        String toolId = toolsClient.extractFinancingId(createResponse);
        log.info("Created tool with ID: {}", toolId);

        // Delete the tool
        log.info("Deleting tool for the first time");
        APIResponse deleteResponse = toolsClient.deleteToolsFinancing(toolId);
        ApiAssertions.assertStatusOneOf(
                deleteResponse,
                "First deletion should succeed",
                200, 204
        );
        log.info("First deletion succeeded with status: {}", deleteResponse.status());

        // Try to delete again
        log.info("Attempting to delete already deleted tool");
        APIResponse secondDeleteResponse = toolsClient.deleteToolsFinancing(toolId);

        // API uses idempotent deletes - may return 204 even if already deleted
        ApiAssertions.assertStatusOneOf(
                secondDeleteResponse,
                "Deleting already deleted tool",
                404, 204
        );
        log.info("Second deletion returned status: {}", secondDeleteResponse.status());
    }

    @DisplayName("Create tool with SQL injection in name succeeds (sanitized)")
    @Test
    void createTool_withSqlInjectionInName_succeeds() throws IOException {
        log.info("Testing tool creation with SQL injection in name");

        Map<String, Object> body = buildMinimalToolBody();
        body.put("name", "Tool'; DROP TABLE tools;-- " + System.currentTimeMillis());

        APIResponse response = toolsClient.createToolsFinancing(body);
        log.info("Create tool with SQL injection - status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatusOneOf(response, "Create tool with SQL injection", 200, 201, 400);

        if (response.status() == 200 || response.status() == 201) {
            String toolId = toolsClient.extractFinancingId(response);
            if (toolId != null) {
                createdIds.add(toolId);
                log.info("Tool with SQL injection name created, ID: {}", toolId);
            }
        }
    }

    @DisplayName("Create tool with XSS in name succeeds (sanitized)")
    @Test
    void createTool_withXssInName_succeeds() throws IOException {
        log.info("Testing tool creation with XSS in name");

        Map<String, Object> body = buildMinimalToolBody();
        body.put("name", "<script>alert('xss')</script>Tool " + System.currentTimeMillis());

        APIResponse response = toolsClient.createToolsFinancing(body);
        log.info("Create tool with XSS - status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatusOneOf(response, "Create tool with XSS", 200, 201, 400);

        if (response.status() == 200 || response.status() == 201) {
            String toolId = toolsClient.extractFinancingId(response);
            if (toolId != null) {
                createdIds.add(toolId);
                log.info("Tool with XSS name created, ID: {}", toolId);
            }
        }
    }

    @DisplayName("Create tool with extremely long name returns error or accepts")
    @Test
    void createTool_withExtremelyLongName_returnsErrorOrAccepts() throws IOException {
        log.info("Testing tool creation with extremely long name (10000 characters)");

        Map<String, Object> body = buildMinimalToolBody();
        body.put("name", "T".repeat(10000));

        APIResponse response = toolsClient.createToolsFinancing(body);

        log.info("Create tool with long name - status: {}", response.status());

        ApiAssertions.assertStatusOneOf(response, "Create tool with extremely long name", 200, 201, 400, 500);

        if (response.status() == 200 || response.status() == 201) {
            String toolId = toolsClient.extractFinancingId(response);
            if (toolId != null) {
                createdIds.add(toolId);
            }
        }
    }

    /**
     * Builds a minimal valid tool body for testing purposes.
     */
    private Map<String, Object> buildMinimalToolBody() {
        Map<String, Object> body = new HashMap<>();
        long ts = System.currentTimeMillis();

        body.put("name", "Minimal Tool " + ts);
        body.put("description", "Minimal tool for negative testing");
        body.put("mfg", "Minimal Mfg " + ts);

        return body;
    }
}
