package org.example.fixtures;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import org.example.Api.helpers.SupplierHelper.SuppliersClient;
import org.example.apifactories.SuppliersTestDataFactory;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.util.Map;

/**
 * Reusable fixture: creates a supplier via API and deletes it in cleanup().
 * Intended to be reused across UI/API tests.
 */
public class SupplierApiFixture {

    private final SuppliersClient suppliersClient;

    private String supplierId;
    private String supplierName;

    private SupplierApiFixture(SuppliersClient suppliersClient) {
        this.suppliersClient = suppliersClient;
    }

    public static SupplierApiFixture create(APIRequestContext requestContext) {
        return new SupplierApiFixture(new SuppliersClient(requestContext));
    }

    /**
     * Creates supplier using SuppliersTestDataFactory and extracts id + businessName.
     *
     * @param namePrefix prefix used by SuppliersTestDataFactory
     */
    public SupplierApiFixture provisionSupplier(String namePrefix) throws IOException {
        Map<String, Object> body = SuppliersTestDataFactory.buildCreateSupplierRequest(namePrefix);

        APIResponse resp = suppliersClient.createSupplier(body);
        assertStatus(resp, "CREATE SUPPLIER", 200, 201);

        supplierId = suppliersClient.extractSupplierId(resp);
        assertNotBlank("supplierId", supplierId);

        // supplierName: prefer response businessName, fallback to request body
        supplierName = extractSupplierName(resp, body);
        assertNotBlank("supplierName", supplierName);

        return this;
    }

    /**
     * Deletes created supplier if present.
     * If supplier is referenced (e.g. by PO) backend may return 409 — handle this outside or after deleting references.
     */
    public void cleanup(String reason) {
        if (!isNotBlank(supplierId)) return;

        try {
            APIResponse resp = suppliersClient.deleteSupplier(supplierId);

            // reason param is here for consistency with other fixtures; if your API doesn't accept reason - we just ignore it
            // (we don't send it anywhere because SuppliersClient.deleteSupplier() signature doesn't accept it)

            assertStatus(resp, "DELETE SUPPLIER", 200, 204);
        } catch (Exception e) {
            System.out.println("DELETE SUPPLIER failed: " + e.getMessage());
        }
    }

    public String supplierId() {
        return supplierId;
    }

    public String supplierName() {
        return supplierName;
    }

    // -------------------------
    // Helpers
    // -------------------------

    private String extractSupplierName(APIResponse resp, Map<String, Object> requestBody) {
        try {
            JsonNode supplierNode = suppliersClient.extractSupplierNode(resp);
            if (supplierNode != null && supplierNode.get("businessName") != null && !supplierNode.get("businessName").isNull()) {
                String name = supplierNode.get("businessName").asText();
                if (isNotBlank(name)) return name;
            }
        } catch (Exception ignored) {
            // fallback below
        }

        Object fromBody = requestBody.get("businessName");
        return fromBody == null ? null : String.valueOf(fromBody);
    }

    private static void assertStatus(APIResponse resp, String step, int... okStatuses) {
        int actual = resp.status();
        for (int s : okStatuses) {
            if (actual == s) return;
        }
        Assertions.fail(step + " unexpected status: " + actual + ", body: " + resp.text());
    }

    private static void assertNotBlank(String field, String value) {
        Assertions.assertNotNull(value, field + " is null");
        Assertions.assertFalse(value.isBlank(), field + " is blank");
    }

    private static boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }
}
