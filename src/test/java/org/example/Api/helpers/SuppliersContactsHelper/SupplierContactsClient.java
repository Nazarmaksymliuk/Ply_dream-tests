package org.example.Api.helpers.SuppliersContactsHelper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;

import java.io.IOException;

public class SupplierContactsClient {

    private final APIRequestContext request;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SupplierContactsClient(APIRequestContext request) {
        this.request = request;
    }

    // GET /supplier-contacts
    public APIResponse getAllSupplierContacts() {
        return request.get("/supplier-contacts");
    }

    public JsonNode parseResponse(APIResponse response) throws IOException {
        return objectMapper.readTree(response.text());
    }

    /**
     * Повертає id першого суплаєра.
     * Зроблено максимально універсально:
     *  - якщо root = [ {..}, {..} ] → беремо root[0].id
     *  - якщо root = { "content": [ {..}, .. ] } → беремо content[0].id
     */
    public String extractFirstSupplierId(JsonNode root) {
        JsonNode first = null;

        if (root.isArray() && root.size() > 0) {
            first = root.get(0);
        } else if (root.has("content") && root.get("content").isArray()
                && root.get("content").size() > 0) {
            first = root.get("content").get(0);
        }

        if (first == null) {
            return null;
        }

        JsonNode idNode = first.get("id");
        return (idNode == null || idNode.isNull()) ? null : idNode.asText();
    }
}
