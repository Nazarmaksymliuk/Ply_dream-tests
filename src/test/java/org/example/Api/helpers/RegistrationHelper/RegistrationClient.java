package org.example.Api.helpers.RegistrationHelper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;

import java.io.IOException;
import java.util.Map;

public class RegistrationClient {

    private final APIRequestContext request;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RegistrationClient(APIRequestContext request) {
        this.request = request;
    }

    // =========================
    // EXISTING METHODS (DO NOT TOUCH)
    // =========================

    // POST /register
    public APIResponse registerBusiness(Map<String, Object> body) {
        String url = "/register";
        return request.post(url, RequestOptions.create().setData(body));
    }

    // DELETE /businesses/admin/{businessId}
    public APIResponse deleteBusinessAsAdmin(String businessId) {
        String url = "/businesses/admin/" + businessId;
        return request.delete(url);
    }

    public JsonNode parseRegistrationResponse(APIResponse response) throws IOException {
        return objectMapper.readTree(response.text());
    }

    public String extractBusinessId(APIResponse response) throws IOException {
        JsonNode root = parseRegistrationResponse(response);
        JsonNode idNode = root.get("businessId");
        return (idNode == null || idNode.isNull()) ? null : idNode.asText();
    }

    // =========================
    // NEW METHODS (ADMIN)
    // =========================

    /**
     * POST /admin/register
     * Requires adminApi context (Authorization: Bearer <adminToken>)
     */
    public APIResponse registerBusinessByAdmin(Map<String, Object> body) {
        String url = "/admin/register";
        return request.post(url, RequestOptions.create().setData(body));
    }
}
