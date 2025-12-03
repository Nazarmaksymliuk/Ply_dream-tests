package org.example.Api.Suppliers;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.APIResponse;
import org.example.Api.helpers.SupplierHelper.SuppliersClient;
import org.example.BaseAPITestExtension.BaseApiTest;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SupplierE2ETests extends BaseApiTest {

    private SuppliersClient suppliersClient;
    private String supplierId;

    @BeforeAll
    void initClient() {
        suppliersClient = new SuppliersClient(userApi);
    }

    @Test
    @Order(1)
    void createSupplier_createsAndStoresId() throws IOException {
        Map<String, Object> body = buildCreateSupplierRequest();

        System.out.println("CREATE SUPPLIER request body: " + body);

        APIResponse response = suppliersClient.createSupplier(body);
        int status = response.status();

        System.out.println("CREATE SUPPLIER status: " + status);
        System.out.println("CREATE SUPPLIER body: " + response.text());

        Assertions.assertTrue(
                status == 200 || status == 201,
                "Expected 200 or 201 on create, but got: " + status
        );

        supplierId = suppliersClient.extractSupplierId(response);
        Assertions.assertNotNull(supplierId, "supplierId must not be null after create");
        Assertions.assertFalse(supplierId.isEmpty(), "supplierId must not be empty");
    }

    @Test
    @Order(2)
    void updateSupplier_updatesPreviouslyCreated() throws IOException {
        Assertions.assertNotNull(supplierId, "supplierId is null – create test probably failed");

        Map<String, Object> updateBody = buildUpdateSupplierRequest(supplierId);

        APIResponse response = suppliersClient.updateSupplier(supplierId, updateBody);
        int status = response.status();

        System.out.println("UPDATE SUPPLIER status: " + status);
        System.out.println("UPDATE SUPPLIER body: " + response.text());

        Assertions.assertEquals(200, status, "Expected 200 on update");

        JsonNode supplierNode = suppliersClient.extractSupplierNode(response);
        Assertions.assertNotNull(supplierNode, "Updated response should contain supplier JSON");

        String actualBusinessName = supplierNode.get("businessName").asText();
        String expectedBusinessName = (String) updateBody.get("businessName");
        Assertions.assertEquals(expectedBusinessName, actualBusinessName);

        String actualBusinessEmail = supplierNode.get("businessEmail").asText();
        String expectedBusinessEmail = (String) updateBody.get("businessEmail");
        Assertions.assertEquals(expectedBusinessEmail, actualBusinessEmail);
    }

    @Test
    @Order(3)
    void deleteSupplier_deletesPreviouslyCreated() {
        Assertions.assertNotNull(supplierId, "supplierId is null – create test probably failed");

        APIResponse response = suppliersClient.deleteSupplier(supplierId);
        int status = response.status();

        System.out.println("DELETE SUPPLIER status: " + status);
        System.out.println("DELETE SUPPLIER body: " + response.text());

        Assertions.assertTrue(
                status == 200 || status == 204,
                "Expected 200 or 204 on delete, but got: " + status
        );
    }

    // ---------- helpers ----------

    // ---------- helpers ----------

    private static Map<String, Object> buildCreateSupplierRequest() {
        long ts = System.currentTimeMillis();
        Random random = new Random();

        // Внутрішній DTO (один supplier)
        Map<String, Object> supplierDto = new HashMap<>();
        supplierDto.put("address", "742 Evergreen Terrace");
        supplierDto.put("businessEmail", "contact+" + ts + "@northstar-transport.com");
        supplierDto.put("businessName", "API North Star Transport " + ts);
        supplierDto.put("city", "Denver");
        supplierDto.put("contactName", "Olivia Carter " + random.nextInt(1000));
        supplierDto.put("finishOnboarding", false);
        supplierDto.put("hphSupplier", false);
        supplierDto.put("note", "Priority partner for refrigerated shipments. Created via API E2E test " + ts);
        supplierDto.put("paymentAccountId", "acc_" + ts);
        supplierDto.put("paymentOnboarded", false);
        supplierDto.put("phoneNumber", "+1415555" + (1000 + random.nextInt(8999)));
        supplierDto.put("profileImage", "https://example.com/logo.png");
        supplierDto.put("sendInvitationEmails", false);
        supplierDto.put("state", "COLORADO");
        supplierDto.put("tags", List.of("refrigerated", "priority", "api-test"));
        supplierDto.put("usZipCode", "80202");
        supplierDto.put("website", "https://www.northstar-transport.com");

        // Обгортка, яку реально чекає бекенд
        Map<String, Object> body = new HashMap<>();
        body.put("supplierRequestDtos", List.of(supplierDto));

        return body;
    }

    private static Map<String, Object> buildUpdateSupplierRequest(String supplierId) {
        Map<String, Object> body = new HashMap<>();
        long ts = System.currentTimeMillis();

        // обовʼязкові поля – ті, на які бек свариться
        body.put("businessName", "API CRUD Supplier UPDATED " + ts);
        body.put("businessEmail", "api-crud-supplier-upd-" + ts + "@example.com");
        body.put("contactName", "API CRUD Contact UPDATED " + ts);

        body.put("phoneNumber", "+14155550199");
        body.put("city", "Lviv");
        body.put("note", "Updated via Supplier CRUD API test at " + ts);

        body.put("address", "Updated street 2");
        body.put("state", "COLORADO");      // підстав валідне значення з enum’у
        body.put("usZipCode", "54321");
        body.put("website", "https://www.updated-supplier.com");
        body.put("profileImage", "https://example.com/logo-updated.png");
        body.put("paymentAccountId", "acc_upd_" + ts);

        body.put("finishOnboarding", false);
        body.put("paymentOnboarded", false);
        body.put("sendInvitationEmails", false);
        body.put("sampleData", false);
        body.put("hphSupplier", false);
        body.put("editable", true);
        body.put("verified", false);

        body.put("supplierAdditionalContactInfos", List.of());

        return body;
    }


}
