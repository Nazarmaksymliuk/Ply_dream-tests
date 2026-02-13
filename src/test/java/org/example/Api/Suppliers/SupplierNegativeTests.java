package org.example.Api.Suppliers;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.example.Api.helpers.SupplierHelper.SuppliersClient;
import org.example.Api.helpers.assertions.ApiAssertions;
import org.example.BaseAPITestExtension.BaseApiTest;
import org.example.apifactories.SuppliersTestDataFactory;
import org.example.config.TestEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Epic("Suppliers")
@Feature("Suppliers Negative Scenarios")
@Timeout(value = TestEnvironment.E2E_TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
public class SupplierNegativeTests extends BaseApiTest {

    private static final Logger logger = LoggerFactory.getLogger(SupplierNegativeTests.class);
    private final SuppliersClient suppliersClient = new SuppliersClient();

    @Test
    @DisplayName("Create supplier with empty body should return 400")
    public void createSupplier_withEmptyBody_returns400() {
        logger.info("Test: Create supplier with empty body");

        Map<String, Object> emptyBody = new HashMap<>();

        Response response = suppliersClient.createSupplier(emptyBody);
        logger.info("Response status: {}", response.getStatusCode());

        ApiAssertions.assertStatus(400, response, "Create supplier with empty body");
    }

    @Test
    @DisplayName("Get supplier with non-existent ID should return 404")
    public void getSupplier_withNonExistentId_returns404() {
        logger.info("Test: Get supplier with non-existent ID");

        String nonExistentId = UUID.randomUUID().toString();
        logger.info("Using non-existent ID: {}", nonExistentId);

        Response response = suppliersClient.getSupplier(nonExistentId);
        logger.info("Response status: {}", response.getStatusCode());

        ApiAssertions.assertStatus(404, response, "Get supplier with non-existent ID");
    }

    @Test
    @DisplayName("Get supplier with invalid ID format should return 400 or 404")
    public void getSupplier_withInvalidIdFormat_returns400or404() {
        logger.info("Test: Get supplier with invalid ID format");

        String invalidId = "invalid-id";
        logger.info("Using invalid ID: {}", invalidId);

        Response response = suppliersClient.getSupplier(invalidId);
        logger.info("Response status: {}", response.getStatusCode());

        ApiAssertions.assertStatusOneOf(response, "Get supplier with invalid ID format", 400, 404);
    }

    @Test
    @DisplayName("Update supplier with non-existent ID should return 404")
    public void updateSupplier_withNonExistentId_returns404() {
        logger.info("Test: Update supplier with non-existent ID");

        String nonExistentId = UUID.randomUUID().toString();
        logger.info("Using non-existent ID: {}", nonExistentId);

        Map<String, Object> updateBody = SuppliersTestDataFactory.buildCreateSupplierRequest("UpdateTest");
        Response response = suppliersClient.updateSupplier(nonExistentId, updateBody);
        logger.info("Response status: {}", response.getStatusCode());

        ApiAssertions.assertStatus(404, response, "Update supplier with non-existent ID");
    }

    @Test
    @DisplayName("Delete supplier with non-existent ID should return 404")
    public void deleteSupplier_withNonExistentId_returns404() {
        logger.info("Test: Delete supplier with non-existent ID");

        String nonExistentId = UUID.randomUUID().toString();
        logger.info("Using non-existent ID: {}", nonExistentId);

        Response response = suppliersClient.deleteSupplier(nonExistentId);
        logger.info("Response status: {}", response.getStatusCode());

        ApiAssertions.assertStatus(404, response, "Delete supplier with non-existent ID");
    }

    @Test
    @DisplayName("Delete already deleted supplier should return 404")
    public void deleteSupplier_alreadyDeleted_returns404() {
        logger.info("Test: Delete already deleted supplier");

        // Create a supplier
        Map<String, Object> createBody = SuppliersTestDataFactory.buildCreateSupplierRequest("DeleteTest");
        Response createResponse = suppliersClient.createSupplier(createBody);
        Map<String, Object> createdSupplier = ApiAssertions.assertSuccessAndParse(createResponse, "Create supplier for delete test");
        String supplierId = (String) createdSupplier.get("id");
        logger.info("Created supplier with ID: {}", supplierId);

        // Delete the supplier
        Response firstDeleteResponse = suppliersClient.deleteSupplier(supplierId);
        logger.info("First delete response status: {}", firstDeleteResponse.getStatusCode());
        ApiAssertions.assertStatusOneOf(firstDeleteResponse, "First delete of supplier", 200, 204);

        // Try to delete the same supplier again
        Response secondDeleteResponse = suppliersClient.deleteSupplier(supplierId);
        logger.info("Second delete response status: {}", secondDeleteResponse.getStatusCode());

        ApiAssertions.assertStatus(404, secondDeleteResponse, "Delete already deleted supplier");
    }

    @Test
    @DisplayName("Create supplier with SQL injection in name should succeed")
    public void createSupplier_withSqlInjectionInName_succeeds() {
        logger.info("Test: Create supplier with SQL injection in name");

        String sqlInjectionName = "Supplier'; DROP TABLE suppliers;--";
        Map<String, Object> createBody = SuppliersTestDataFactory.buildCreateSupplierRequest("SqlInjection");
        createBody.put("name", sqlInjectionName);

        Response response = suppliersClient.createSupplier(createBody);
        logger.info("Response status: {}", response.getStatusCode());

        Map<String, Object> createdSupplier = ApiAssertions.assertSuccessAndParse(response, "Create supplier with SQL injection in name");
        String supplierId = (String) createdSupplier.get("id");
        logger.info("Created supplier with ID: {} and SQL injection in name", supplierId);

        // Clean up
        try {
            suppliersClient.deleteSupplier(supplierId);
            logger.info("Cleaned up supplier with ID: {}", supplierId);
        } catch (Exception e) {
            logger.warn("Failed to clean up supplier with ID: {}", supplierId, e);
        }
    }

    @Test
    @DisplayName("Create supplier with XSS in name should succeed")
    public void createSupplier_withXssInName_succeeds() {
        logger.info("Test: Create supplier with XSS in name");

        String xssName = "<script>alert('xss')</script>";
        Map<String, Object> createBody = SuppliersTestDataFactory.buildCreateSupplierRequest("XssTest");
        createBody.put("name", xssName);

        Response response = suppliersClient.createSupplier(createBody);
        logger.info("Response status: {}", response.getStatusCode());

        Map<String, Object> createdSupplier = ApiAssertions.assertSuccessAndParse(response, "Create supplier with XSS in name");
        String supplierId = (String) createdSupplier.get("id");
        logger.info("Created supplier with ID: {} and XSS in name", supplierId);

        // Clean up
        try {
            suppliersClient.deleteSupplier(supplierId);
            logger.info("Cleaned up supplier with ID: {}", supplierId);
        } catch (Exception e) {
            logger.warn("Failed to clean up supplier with ID: {}", supplierId, e);
        }
    }
}
