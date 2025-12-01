package org.example.Api.helpers.ToolsHelper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;

import java.io.IOException;
import java.util.Map;

public class ToolsClient {

    private final APIRequestContext request;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ToolsClient(APIRequestContext request) {
        this.request = request;
    }

    // POST /materials-financings/tools
    public APIResponse createToolsFinancing(Map<String, Object> body) {
        return request.post(
                "/materials-financings/tools",
                RequestOptions.create().setData(body)
        );
    }

    // PUT /materials-financings/tools/{id}
    public APIResponse updateToolsFinancing(String id, Map<String, Object> body) {
        String url = "/materials-financings/tools/" + id;
        return request.put(
                url,
                RequestOptions.create().setData(body)
        );
    }

    // DELETE /materials-financings/tools/{id}
    public APIResponse deleteToolsFinancing(String id) {
        String url = "/materials-financings/tools/" + id;
        return request.delete(url);
    }

    // ---------- helpers ----------

    public String extractFinancingId(APIResponse response) throws IOException {
        String json = response.text();
        JsonNode root = objectMapper.readTree(json);
        JsonNode idNode = root.get("id");
        return (idNode == null || idNode.isNull()) ? null : idNode.asText();
    }

    public JsonNode parseFinancing(APIResponse response) throws IOException {
        return objectMapper.readTree(response.text());
    }

    public JsonNode getFirstToolUnit(JsonNode root) {
        JsonNode arr = root.get("toolUnits");
        if (arr == null || !arr.isArray() || arr.isEmpty()) {
            return null;
        }
        return arr.get(0);
    }
}
