package org.example.Api.Suppliers;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.APIResponse;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.example.Api.helpers.SupplierHelper.SuppliersClient;
import org.example.BaseAPITestExtension.BaseApiTest;
import org.example.apifactories.SuppliersTestDataFactory;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.example.config.TestEnvironment;
import org.junit.jupiter.api.Timeout;

@Epic("Suppliers")
@Feature("Suppliers E2E CRUD")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Timeout(value = TestEnvironment.E2E_TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
public class SupplierE2ETests extends BaseApiTest {

    private static final Logger log = LoggerFactory.getLogger(SupplierE2ETests.class);

    private SuppliersClient suppliersClient;
    private String supplierId;

    @BeforeAll
    void initClient() {
        suppliersClient = new SuppliersClient(userApi);
    }

    @DisplayName("Create Supplier")
    @Test
    @Order(1)
    void createSupplier_createsAndStoresId() throws IOException {
        Map<String, Object> body = SuppliersTestDataFactory.buildCreateSupplierRequest(
                "API North Star Transport "
        );

        log.debug("CREATE SUPPLIER request body: {}", body);

        APIResponse response = suppliersClient.createSupplier(body);
        int status = response.status();

        log.info("CREATE SUPPLIER status: {}", status);
        log.debug("CREATE SUPPLIER body: {}", response.text());

        Assertions.assertTrue(
                status == 200 || status == 201,
                "Expected 200 or 201 on create, but got: " + status
        );

        supplierId = suppliersClient.extractSupplierId(response);
        Assertions.assertNotNull(supplierId, "supplierId must not be null after create");
        Assertions.assertFalse(supplierId.isEmpty(), "supplierId must not be empty");
    }

    @DisplayName("Update Supplier")
    @Test
    @Order(2)
    void updateSupplier_updatesPreviouslyCreated() throws IOException {
        Assertions.assertNotNull(supplierId, "supplierId is null – create test probably failed");

        Map<String, Object> updateBody = SuppliersTestDataFactory.buildUpdateSupplierRequest(
                supplierId,
                "API CRUD Supplier UPDATED "
        );

        APIResponse response = suppliersClient.updateSupplier(supplierId, updateBody);
        int status = response.status();

        log.info("UPDATE SUPPLIER status: {}", status);
        log.debug("UPDATE SUPPLIER body: {}", response.text());

        Assertions.assertEquals(200, status, "Expected 200 on update");

        JsonNode supplierNode = suppliersClient.extractSupplierNode(response);
        Assertions.assertNotNull(supplierNode, "Updated response should contain supplier JSON");

        Assertions.assertEquals(
                updateBody.get("businessName"),
                supplierNode.get("businessName").asText()
        );

        Assertions.assertEquals(
                updateBody.get("businessEmail"),
                supplierNode.get("businessEmail").asText()
        );
    }

    @DisplayName("Delete Supplier")
    @Test
    @Order(3)
    void deleteSupplier_deletesPreviouslyCreated() {
        Assertions.assertNotNull(supplierId, "supplierId is null – create test probably failed");

        APIResponse response = suppliersClient.deleteSupplier(supplierId);
        int status = response.status();

        log.info("DELETE SUPPLIER status: {}", status);
        log.debug("DELETE SUPPLIER body: {}", response.text());

        Assertions.assertTrue(
                status == 200 || status == 204,
                "Expected 200 or 204 on delete, but got: " + status
        );
    }
}
