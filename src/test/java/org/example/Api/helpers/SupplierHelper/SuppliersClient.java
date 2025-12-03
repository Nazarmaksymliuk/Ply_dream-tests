package org.example.Api.helpers.SupplierHelper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;

import java.io.IOException;
import java.util.Map;

public class SuppliersClient {

    private final APIRequestContext request;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SuppliersClient(APIRequestContext request) {
        this.request = request;
    }

    // ---------- CRUD ----------

    // POST /supplier-contacts
    public APIResponse createSupplier(Map<String, Object> body) {
        return request.post(
                "/supplier-contacts",
                RequestOptions.create().setData(body)
        );
    }

    // PUT /supplier-contacts/{id}
    public APIResponse updateSupplier(String supplierId, Map<String, Object> body) {
        return request.put(
                "/supplier-contacts/" + supplierId,
                RequestOptions.create().setData(body)
        );
    }

    // DELETE /supplier-contacts/{id}
    public APIResponse deleteSupplier(String supplierId) {
        return request.delete(
                "/supplier-contacts/" + supplierId
        );
    }

    // (опціонально) GET /supplier-contacts/{id}
    public APIResponse getSupplier(String supplierId) {
        return request.get(
                "/supplier-contacts/" + supplierId
        );
    }

    // ---------- helpers ----------

    /**
     * Витягує JSON supplier’a з відповіді.
     * Якщо бекенд повертає масив (як у твоєму прикладі GET /supplier-contacts),
     * беремо перший елемент. Якщо об’єкт — повертаємо його.
     */
    public JsonNode extractSupplierNode(APIResponse response) throws IOException {
        String json = response.text();
        JsonNode root = objectMapper.readTree(json);

        if (root.isObject()) {
            return root;
        }
        if (root.isArray() && root.size() > 0) {
            return root.get(0);
        }
        return null;
    }

    /**
     * Витягує id supplier’a з відповіді create/update/GET.
     */
    public String extractSupplierId(APIResponse response) throws IOException {
        JsonNode supplierNode = extractSupplierNode(response);
        if (supplierNode == null || supplierNode.get("id") == null) {
            return null;
        }
        return supplierNode.get("id").asText();
    }
}
