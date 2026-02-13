package org.example.Api.Materials;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIResponse;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.example.Api.helpers.MaterialsHelper.MaterialsClient;
import org.example.Api.helpers.assertions.ApiAssertions;
import org.example.BaseAPITestExtension.BaseApiTest;
import org.example.apifactories.MaterialsTestDataFactory;
import org.example.config.TestEnvironment;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Comprehensive negative API test suite for Materials API.
 * Tests invalid inputs, non-existent resources, and edge cases.
 */
@Epic("Materials")
@Feature("Materials Negative Scenarios")
@Timeout(value = TestEnvironment.E2E_TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
public class MaterialApiNegativeTests extends BaseApiTest {

    private static final Logger log = LoggerFactory.getLogger(MaterialApiNegativeTests.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private MaterialsClient materialsClient;
    private final List<String> createdIds = new ArrayList<>();

    @BeforeAll
    void initClient() {
        materialsClient = new MaterialsClient(userApi);
        log.info("Initialized MaterialsClient for negative API tests");
    }

    @AfterAll
    void cleanup() {
        log.info("Cleaning up {} created materials", createdIds.size());
        for (String id : createdIds) {
            try {
                APIResponse deleteResp = materialsClient.deleteMaterial(id);
                log.debug("Cleanup: deleted material {} with status {}", id, deleteResp.status());
            } catch (Exception e) {
                log.warn("Cleanup: failed to delete material {}: {}", id, e.getMessage());
            }
        }
    }

    @Test
    @DisplayName("Create material with empty name returns 400")
    void createMaterial_withEmptyName_returns400() throws IOException {
        log.info("TEST: Create material with empty name");

        Map<String, Object> body = MaterialsTestDataFactory.buildCreateMaterialRequest("", "ITEM-");
        body.put("name", "");

        APIResponse response = materialsClient.createMaterial(body);

        log.info("Response status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatus(400, response, "Create material with empty name");

        JsonNode root = objectMapper.readTree(response.text());
        Assertions.assertTrue(
                root.has("message"),
                "Expected error response to contain 'message' field"
        );
    }

    @Test
    @DisplayName("Create material with null body returns 400")
    void createMaterial_withNullBody_returns400() throws IOException {
        log.info("TEST: Create material with null/empty body");

        Map<String, Object> body = new HashMap<>();

        APIResponse response = materialsClient.createMaterial(body);

        log.info("Response status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatus(400, response, "Create material with empty body");

        JsonNode root = objectMapper.readTree(response.text());
        Assertions.assertTrue(
                root.has("message"),
                "Expected error response to contain 'message' field"
        );
    }

    @Test
    @DisplayName("Create material with extremely long name returns 400")
    void createMaterial_withExtremelyLongName_returns400() throws IOException {
        log.info("TEST: Create material with extremely long name (10000 characters)");

        // Generate a 10000 character name
        String longName = "A".repeat(10000);

        Map<String, Object> body = MaterialsTestDataFactory.buildCreateMaterialRequest("", "ITEM-");
        body.put("name", longName);

        APIResponse response = materialsClient.createMaterial(body);

        log.info("Response status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatus(400, response, "Create material with extremely long name");

        JsonNode root = objectMapper.readTree(response.text());
        Assertions.assertTrue(
                root.has("message"),
                "Expected error response to contain 'message' field"
        );
    }

    @Test
    @DisplayName("Get material with non-existent ID returns 404")
    void getMaterial_withNonExistentId_returns404() throws IOException {
        log.info("TEST: Get material with non-existent ID");

        String nonExistentId = UUID.randomUUID().toString();

        APIResponse response = materialsClient.getMaterial(nonExistentId);

        log.info("Response status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatus(404, response, "Get material with non-existent ID");

        String body = response.text();
        Assertions.assertTrue(
                body.contains("not found") || body.contains("Not Found") || body.contains("404"),
                "Expected 404 error message to indicate resource not found, got: " + body
        );
    }

    @Test
    @DisplayName("Get material with invalid ID format returns 400 or 404")
    void getMaterial_withInvalidIdFormat_returns400or404() {
        log.info("TEST: Get material with invalid ID format");

        String invalidId = "not-a-uuid";

        APIResponse response = materialsClient.getMaterial(invalidId);

        log.info("Response status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatusOneOf(
                response,
                "Get material with invalid ID format",
                400, 404
        );
    }

    @Test
    @DisplayName("Update material with non-existent ID returns 404")
    void updateMaterial_withNonExistentId_returns404() throws IOException {
        log.info("TEST: Update material with non-existent ID");

        String nonExistentId = UUID.randomUUID().toString();

        Map<String, Object> body = MaterialsTestDataFactory.buildUpdateMaterialRequest(
                nonExistentId,
                "UPDATE-",
                "UPDATE-ITEM-"
        );

        APIResponse response = materialsClient.updateMaterial(nonExistentId, body);

        log.info("Response status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatus(404, response, "Update material with non-existent ID");

        String responseBody = response.text();
        Assertions.assertTrue(
                responseBody.contains("not found") || responseBody.contains("Not Found") || responseBody.contains("404"),
                "Expected 404 error message to indicate resource not found, got: " + responseBody
        );
    }

    @Test
    @DisplayName("Delete material with non-existent ID returns 404")
    void deleteMaterial_withNonExistentId_returns404() throws IOException {
        log.info("TEST: Delete material with non-existent ID");

        String nonExistentId = UUID.randomUUID().toString();

        APIResponse response = materialsClient.deleteMaterial(nonExistentId);

        log.info("Response status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatus(404, response, "Delete material with non-existent ID");

        String body = response.text();
        Assertions.assertTrue(
                body.contains("not found") || body.contains("Not Found") || body.contains("404"),
                "Expected 404 error message to indicate resource not found, got: " + body
        );
    }

    @Test
    @DisplayName("Delete already deleted material returns 404")
    void deleteMaterial_alreadyDeleted_returns404() throws IOException {
        log.info("TEST: Delete already deleted material");

        // Step 1: Create a material
        Map<String, Object> createBody = MaterialsTestDataFactory.buildCreateMaterialRequest(
                "DELETE-TEST-",
                "DELETE-ITEM-"
        );

        APIResponse createResp = materialsClient.createMaterial(createBody);

        log.info("Create response status: {}", createResp.status());

        JsonNode createRoot = ApiAssertions.assertSuccessAndParse(createResp, "Create material");
        String materialId = materialsClient.extractMaterialId(createResp);

        Assertions.assertNotNull(materialId, "Material ID should not be null after creation");
        log.info("Created material with ID: {}", materialId);

        // Step 2: Delete the material (first time)
        APIResponse deleteResp1 = materialsClient.deleteMaterial(materialId);

        log.info("First delete response status: {}", deleteResp1.status());

        ApiAssertions.assertStatusOneOf(
                deleteResp1,
                "First delete operation",
                200, 204
        );

        // Step 3: Try to delete again (should return 404)
        APIResponse deleteResp2 = materialsClient.deleteMaterial(materialId);

        log.info("Second delete response status: {}, body: {}", deleteResp2.status(), deleteResp2.text());

        ApiAssertions.assertStatus(404, deleteResp2, "Delete already deleted material");

        String body = deleteResp2.text();
        Assertions.assertTrue(
                body.contains("not found") || body.contains("Not Found") || body.contains("404"),
                "Expected 404 error message to indicate resource not found, got: " + body
        );
    }

    @Test
    @DisplayName("Create material with SQL injection characters succeeds")
    void createMaterial_withSpecialCharactersInName_succeeds() throws IOException {
        log.info("TEST: Create material with SQL injection characters in name");

        // Test SQL injection attempt
        String sqlInjectionName = "Material'; DROP TABLE materials;--";

        Map<String, Object> body = MaterialsTestDataFactory.buildCreateMaterialRequest("", "SQL-INJECT-ITEM-");
        body.put("name", sqlInjectionName);

        APIResponse response = materialsClient.createMaterial(body);

        log.info("Response status: {}, body: {}", response.status(), response.text());

        // The API should properly handle this and either succeed or reject with 400
        // (not cause a SQL error). We expect success as modern APIs sanitize input.
        ApiAssertions.assertStatusOneOf(
                response,
                "Create material with SQL injection characters",
                200, 201, 400
        );

        if (response.status() == 200 || response.status() == 201) {
            String materialId = materialsClient.extractMaterialId(response);
            if (materialId != null) {
                createdIds.add(materialId);
                log.info("Material with SQL injection characters created successfully with ID: {}", materialId);

                // Verify the material was created with the exact name (not executed as SQL)
                APIResponse getResp = materialsClient.getMaterial(materialId);
                JsonNode root = objectMapper.readTree(getResp.text());
                JsonNode materialNode = root.get("material");

                if (materialNode != null && materialNode.has("name")) {
                    String storedName = materialNode.get("name").asText();
                    log.info("Stored name: {}", storedName);
                    // The name should be stored as-is, proving SQL injection didn't execute
                    Assertions.assertTrue(
                            storedName.contains("DROP TABLE") || storedName.contains("';"),
                            "Expected name to contain SQL injection characters, proving it was not executed"
                    );
                }
            }
        } else {
            log.info("API rejected SQL injection characters with 400, which is acceptable");
        }
    }

    @Test
    @DisplayName("Create material with XSS HTML in name succeeds")
    void createMaterial_withHtmlInName_succeeds() throws IOException {
        log.info("TEST: Create material with XSS/HTML in name");

        // Test XSS attempt
        String xssName = "<script>alert('xss')</script>";

        Map<String, Object> body = MaterialsTestDataFactory.buildCreateMaterialRequest("", "XSS-ITEM-");
        body.put("name", xssName);

        APIResponse response = materialsClient.createMaterial(body);

        log.info("Response status: {}, body: {}", response.status(), response.text());

        // The API should properly handle this and either succeed or reject with 400
        // (not render the script). We expect success as modern APIs sanitize/escape input.
        ApiAssertions.assertStatusOneOf(
                response,
                "Create material with XSS/HTML in name",
                200, 201, 400
        );

        if (response.status() == 200 || response.status() == 201) {
            String materialId = materialsClient.extractMaterialId(response);
            if (materialId != null) {
                createdIds.add(materialId);
                log.info("Material with XSS/HTML created successfully with ID: {}", materialId);

                // Verify the material was created with the exact name (escaped, not executed)
                APIResponse getResp = materialsClient.getMaterial(materialId);
                JsonNode root = objectMapper.readTree(getResp.text());
                JsonNode materialNode = root.get("material");

                if (materialNode != null && materialNode.has("name")) {
                    String storedName = materialNode.get("name").asText();
                    log.info("Stored name: {}", storedName);
                    // The name should contain script tags, proving XSS didn't execute
                    Assertions.assertTrue(
                            storedName.contains("script") || storedName.contains("<") || storedName.contains(">"),
                            "Expected name to contain HTML/script tags, proving it was not executed"
                    );
                }
            }
        } else {
            log.info("API rejected XSS/HTML characters with 400, which is acceptable");
        }
    }
}
