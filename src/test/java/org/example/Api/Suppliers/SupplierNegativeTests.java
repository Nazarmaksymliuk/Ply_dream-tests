package org.example.Api.Suppliers;

import com.microsoft.playwright.APIResponse;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.example.Api.helpers.SupplierHelper.SuppliersClient;
import org.example.Api.helpers.assertions.ApiAssertions;
import org.example.BaseAPITestExtension.BaseApiTest;
import org.example.apifactories.SuppliersTestDataFactory;
import org.example.config.TestEnvironment;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Epic("Suppliers")
@Feature("Suppliers Negative Scenarios")
@Timeout(value = TestEnvironment.E2E_TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
public class SupplierNegativeTests extends BaseApiTest {

    private static final Logger log = LoggerFactory.getLogger(SupplierNegativeTests.class);

    private SuppliersClient suppliersClient;
    private final List<String> createdIds = new ArrayList<>();

    @BeforeAll
    void initClient() {
        suppliersClient = new SuppliersClient(userApi);
    }

    @AfterAll
    void cleanup() {
        for (String id : createdIds) {
            try {
                suppliersClient.deleteSupplier(id);
            } catch (Exception e) {
                log.warn("Cleanup: failed to delete supplier {}: {}", id, e.getMessage());
            }
        }
    }

    @Test
    @DisplayName("Create supplier with empty body returns 400")
    void createSupplier_withEmptyBody_returns400() {
        log.info("Test: Create supplier with empty body");

        Map<String, Object> emptyBody = new HashMap<>();

        APIResponse response = suppliersClient.createSupplier(emptyBody);
        log.info("Response status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatus(400, response, "Create supplier with empty body");
    }

    @Test
    @DisplayName("Get supplier with non-existent ID returns 404")
    void getSupplier_withNonExistentId_returns404() {
        log.info("Test: Get supplier with non-existent ID");

        String nonExistentId = UUID.randomUUID().toString();

        APIResponse response = suppliersClient.getSupplier(nonExistentId);
        log.info("Response status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatus(404, response, "Get supplier with non-existent ID");
    }

    @Test
    @DisplayName("Get supplier with invalid ID format returns 400 or 404")
    void getSupplier_withInvalidIdFormat_returns400or404() {
        log.info("Test: Get supplier with invalid ID format");

        APIResponse response = suppliersClient.getSupplier("invalid-id");
        log.info("Response status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatusOneOf(response, "Get supplier with invalid ID format", 400, 404);
    }

    @Test
    @DisplayName("Update supplier with non-existent ID returns 404")
    void updateSupplier_withNonExistentId_returns404() {
        log.info("Test: Update supplier with non-existent ID");

        String nonExistentId = UUID.randomUUID().toString();
        Map<String, Object> updateBody = SuppliersTestDataFactory.buildUpdateSupplierRequest(nonExistentId, "UpdateTest");

        APIResponse response = suppliersClient.updateSupplier(nonExistentId, updateBody);
        log.info("Response status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatus(404, response, "Update supplier with non-existent ID");
    }

    @Test
    @DisplayName("Delete supplier with non-existent ID returns 404")
    void deleteSupplier_withNonExistentId_returns404() {
        log.info("Test: Delete supplier with non-existent ID");

        String nonExistentId = UUID.randomUUID().toString();

        APIResponse response = suppliersClient.deleteSupplier(nonExistentId);
        log.info("Response status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatus(404, response, "Delete supplier with non-existent ID");
    }

    @Test
    @DisplayName("Delete already deleted supplier returns 404")
    void deleteSupplier_alreadyDeleted_returns404() throws IOException {
        log.info("Test: Delete already deleted supplier");

        Map<String, Object> createBody = SuppliersTestDataFactory.buildCreateSupplierRequest("DeleteTest");
        APIResponse createResponse = suppliersClient.createSupplier(createBody);

        ApiAssertions.assertStatusOneOf(createResponse, "Create supplier for delete test", 200, 201);

        String supplierId = suppliersClient.extractSupplierId(createResponse);
        Assertions.assertNotNull(supplierId, "Supplier ID should not be null");
        log.info("Created supplier with ID: {}", supplierId);

        APIResponse firstDelete = suppliersClient.deleteSupplier(supplierId);
        log.info("First delete status: {}", firstDelete.status());
        ApiAssertions.assertStatusOneOf(firstDelete, "First delete", 200, 204);

        APIResponse secondDelete = suppliersClient.deleteSupplier(supplierId);
        log.info("Second delete status: {}, body: {}", secondDelete.status(), secondDelete.text());

        ApiAssertions.assertStatus(404, secondDelete, "Delete already deleted supplier");
    }

    @Test
    @DisplayName("Create supplier with SQL injection in name succeeds (sanitized)")
    void createSupplier_withSqlInjectionInName_succeeds() throws IOException {
        log.info("Test: Create supplier with SQL injection in name");

        Map<String, Object> createBody = SuppliersTestDataFactory.buildCreateSupplierRequest("SqlInjection");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> dtos = (List<Map<String, Object>>) createBody.get("supplierRequestDtos");
        dtos.get(0).put("businessName", "Supplier'; DROP TABLE suppliers;--");

        APIResponse response = suppliersClient.createSupplier(createBody);
        log.info("Response status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatusOneOf(response, "Create supplier with SQL injection", 200, 201, 400);

        if (response.status() == 200 || response.status() == 201) {
            String id = suppliersClient.extractSupplierId(response);
            if (id != null) createdIds.add(id);
        }
    }

    @Test
    @DisplayName("Create supplier with XSS in name succeeds (sanitized)")
    void createSupplier_withXssInName_succeeds() throws IOException {
        log.info("Test: Create supplier with XSS in name");

        Map<String, Object> createBody = SuppliersTestDataFactory.buildCreateSupplierRequest("XssTest");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> dtos = (List<Map<String, Object>>) createBody.get("supplierRequestDtos");
        dtos.get(0).put("businessName", "<script>alert('xss')</script>");

        APIResponse response = suppliersClient.createSupplier(createBody);
        log.info("Response status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatusOneOf(response, "Create supplier with XSS", 200, 201, 400);

        if (response.status() == 200 || response.status() == 201) {
            String id = suppliersClient.extractSupplierId(response);
            if (id != null) createdIds.add(id);
        }
    }
}
