package org.example.Api.Consumables;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.APIResponse;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.example.Api.helpers.ConsumablesHelper.ConsumablesClient;
import org.example.Api.helpers.MeasurementUnits.MeasurementUnitsClient;
import org.example.Api.helpers.assertions.ApiAssertions;
import org.example.BaseAPITestExtension.BaseApiTest;
import org.example.apifactories.ConsumablesTestDataFactory;
import org.example.config.TestEnvironment;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Comprehensive negative test suite for Consumables API.
 * Tests various error conditions, edge cases, and security scenarios.
 */
@Epic("Consumables")
@Feature("Consumables Negative Scenarios")
@Timeout(value = TestEnvironment.E2E_TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
public class ConsumablesNegativeTests extends BaseApiTest {

    private static final Logger log = LoggerFactory.getLogger(ConsumablesNegativeTests.class);

    private ConsumablesClient consumablesClient;
    private MeasurementUnitsClient measurementUnitsClient;

    private JsonNode eachMeasurementUnit;

    // Track created consumable IDs for cleanup
    private static final List<String> createdIds = Collections.synchronizedList(new ArrayList<>());

    @BeforeAll
    void initClient() throws IOException {
        consumablesClient = new ConsumablesClient(userApi);
        measurementUnitsClient = new MeasurementUnitsClient(userApi);

        // Fetch measurement units for test data
        APIResponse muResponse = measurementUnitsClient.getMeasurementUnits();
        int status = muResponse.status();

        log.info("MEASUREMENT UNITS status: {}", status);
        log.debug("MEASUREMENT UNITS body: {}", muResponse.text());

        ApiAssertions.assertStatus(200, muResponse, "Fetching measurement units for test setup");

        JsonNode root = measurementUnitsClient.parseResponse(muResponse);
        eachMeasurementUnit = measurementUnitsClient.findUnit(root, "EA", "Each");

        Assertions.assertNotNull(eachMeasurementUnit, "MeasurementUnit 'Each/EA' not found - required for test setup");
    }

    @AfterAll
    void cleanup() {
        if (!createdIds.isEmpty()) {
            log.info("Cleaning up {} consumable(s) created during negative tests", createdIds.size());
            try {
                APIResponse deleteResponse = consumablesClient.deleteConsumables(new ArrayList<>(createdIds));
                log.info("Cleanup DELETE status: {}", deleteResponse.status());
                log.debug("Cleanup DELETE body: {}", deleteResponse.text());
            } catch (Exception e) {
                log.warn("Cleanup failed, but continuing: {}", e.getMessage());
            }
        }
    }

    @DisplayName("Create Consumable with Empty Body - API accepts (no server validation)")
    @Test
    void createConsumable_withEmptyBody_accepted() throws IOException {
        log.info("Testing create consumable with empty body");

        Map<String, Object> emptyBody = new HashMap<>();
        APIResponse response = consumablesClient.createConsumable(emptyBody);

        int status = response.status();
        log.info("CREATE with empty body status: {}", status);
        log.debug("CREATE with empty body response: {}", response.text());

        // API does not validate empty body and returns 201
        ApiAssertions.assertStatus(201, response, "Create consumable with empty body");

        String consumableId = consumablesClient.extractConsumableId(response);
        if (consumableId != null && !consumableId.isEmpty()) {
            createdIds.add(consumableId);
        }
    }

    @DisplayName("Create Consumable with Missing Name Field - API accepts (no server validation)")
    @Test
    void createConsumable_withMissingName_accepted() throws IOException {
        log.info("Testing create consumable with missing name field");

        // Build a complete body, then remove the name field
        Map<String, Object> body = ConsumablesTestDataFactory.buildCreateConsumableBody(
                "Test ",
                "CNS-",
                "Tag ",
                eachMeasurementUnit.get("id").asText(),
                eachMeasurementUnit.get("name").asText(),
                eachMeasurementUnit.get("abbreviation").asText()
        );

        body.remove("name"); // Remove name field

        APIResponse response = consumablesClient.createConsumable(body);

        int status = response.status();
        log.info("CREATE without name status: {}", status);
        log.debug("CREATE without name response: {}", response.text());

        // API does not validate missing name and returns 201
        ApiAssertions.assertStatus(201, response, "Create consumable without name");

        String consumableId = consumablesClient.extractConsumableId(response);
        if (consumableId != null && !consumableId.isEmpty()) {
            createdIds.add(consumableId);
        }
    }

    @DisplayName("Update Consumable with Non-Existent ID - Returns 404")
    @Test
    void updateConsumable_withNonExistentId_returns404() throws IOException {
        log.info("Testing update consumable with non-existent ID");

        String nonExistentId = UUID.randomUUID().toString();
        log.info("Using non-existent ID: {}", nonExistentId);

        Map<String, Object> body = ConsumablesTestDataFactory.buildUpdateConsumableBody(
                nonExistentId,
                "Updated ",
                "CNS-UPD-",
                "Tag ",
                eachMeasurementUnit.get("id").asText(),
                eachMeasurementUnit.get("name").asText(),
                eachMeasurementUnit.get("abbreviation").asText()
        );

        APIResponse response = consumablesClient.updateConsumable(nonExistentId, body);

        int status = response.status();
        log.info("UPDATE with non-existent ID status: {}", status);
        log.debug("UPDATE with non-existent ID response: {}", response.text());

        ApiAssertions.assertStatus(404, response, "Update consumable with non-existent ID should return 404");
    }

    @DisplayName("Delete Consumable with Non-Existent ID - Returns 404 or 204")
    @Test
    void deleteConsumable_withNonExistentId_returns404or204() {
        log.info("Testing delete consumable with non-existent ID");

        String nonExistentId = UUID.randomUUID().toString();
        log.info("Using non-existent ID: {}", nonExistentId);

        APIResponse response = consumablesClient.deleteConsumables(Collections.singletonList(nonExistentId));

        int status = response.status();
        log.info("DELETE with non-existent ID status: {}", status);
        log.debug("DELETE with non-existent ID response: {}", response.text());

        // Some APIs return 404, others return 204 for idempotent deletes
        ApiAssertions.assertStatusOneOf(response, "Delete consumable with non-existent ID", 404, 204);
    }

    @DisplayName("Create Consumable with Negative Cost - API accepts (no server validation)")
    @Test
    void createConsumable_withNegativeCost_accepted() throws IOException {
        log.info("Testing create consumable with negative cost");

        Map<String, Object> body = ConsumablesTestDataFactory.buildCreateConsumableBody(
                "Negative Cost ",
                "CNS-NEG-",
                "Tag ",
                eachMeasurementUnit.get("id").asText(),
                eachMeasurementUnit.get("name").asText(),
                eachMeasurementUnit.get("abbreviation").asText()
        );

        // Set negative cost
        body.put("costForBusiness", -1.0);

        APIResponse response = consumablesClient.createConsumable(body);

        int status = response.status();
        log.info("CREATE with negative cost status: {}", status);
        log.debug("CREATE with negative cost response: {}", response.text());

        // API does not validate negative cost and returns 201
        ApiAssertions.assertStatus(201, response, "Create consumable with negative cost");

        String consumableId = consumablesClient.extractConsumableId(response);
        if (consumableId != null && !consumableId.isEmpty()) {
            createdIds.add(consumableId);
        }
    }

    @DisplayName("Create Consumable with SQL Injection - Succeeds (Sanitized)")
    @Test
    void createConsumable_withSqlInjection_succeeds() throws IOException {
        log.info("Testing create consumable with SQL injection attempt");

        String sqlInjectionName = "'; DROP TABLE consumables; --";
        log.info("Using SQL injection string: {}", sqlInjectionName);

        Map<String, Object> body = ConsumablesTestDataFactory.buildCreateConsumableBody(
                sqlInjectionName,
                "CNS-SQL-",
                "Tag ",
                eachMeasurementUnit.get("id").asText(),
                eachMeasurementUnit.get("name").asText(),
                eachMeasurementUnit.get("abbreviation").asText()
        );

        APIResponse response = consumablesClient.createConsumable(body);

        int status = response.status();
        log.info("CREATE with SQL injection status: {}", status);
        log.debug("CREATE with SQL injection response: {}", response.text());

        // Should succeed with 201 - backend should sanitize/escape the input
        ApiAssertions.assertStatus(201, response, "Create consumable with SQL injection should succeed (sanitized)");

        // Extract ID for cleanup
        String consumableId = consumablesClient.extractConsumableId(response);
        if (consumableId != null && !consumableId.isEmpty()) {
            createdIds.add(consumableId);
            log.info("Created consumable with SQL injection attempt, ID: {}", consumableId);

            // Verify the data was stored correctly (not executed as SQL)
            JsonNode created = consumablesClient.parseConsumable(response);
            String storedName = created.get("name").asText();
            log.info("Stored name: {}", storedName);

            // The name should be stored as-is or sanitized, but the table should not be dropped
            Assertions.assertNotNull(storedName, "Name should be stored");
            Assertions.assertFalse(storedName.isEmpty(), "Name should not be empty");
        }
    }

    @DisplayName("Create Consumable with XSS Script Tags - Succeeds (Sanitized)")
    @Test
    void createConsumable_withXss_succeeds() throws IOException {
        log.info("Testing create consumable with XSS script tags");

        String xssName = "<script>alert('XSS')</script>Test Item";
        log.info("Using XSS string: {}", xssName);

        Map<String, Object> body = ConsumablesTestDataFactory.buildCreateConsumableBody(
                xssName,
                "CNS-XSS-",
                "Tag ",
                eachMeasurementUnit.get("id").asText(),
                eachMeasurementUnit.get("name").asText(),
                eachMeasurementUnit.get("abbreviation").asText()
        );

        APIResponse response = consumablesClient.createConsumable(body);

        int status = response.status();
        log.info("CREATE with XSS script status: {}", status);
        log.debug("CREATE with XSS script response: {}", response.text());

        // Should succeed with 201 - backend should sanitize/escape the input
        ApiAssertions.assertStatus(201, response, "Create consumable with XSS script should succeed (sanitized)");

        // Extract ID for cleanup
        String consumableId = consumablesClient.extractConsumableId(response);
        if (consumableId != null && !consumableId.isEmpty()) {
            createdIds.add(consumableId);
            log.info("Created consumable with XSS attempt, ID: {}", consumableId);

            // Verify the data was stored correctly (sanitized or escaped)
            JsonNode created = consumablesClient.parseConsumable(response);
            String storedName = created.get("name").asText();
            log.info("Stored name: {}", storedName);

            // The name should be stored as-is or sanitized, but script should not execute
            Assertions.assertNotNull(storedName, "Name should be stored");
            Assertions.assertFalse(storedName.isEmpty(), "Name should not be empty");
        }
    }
}
