package org.example.Api.helpers.ToolUnits;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;

import java.io.IOException;

public class ToolUnitsClient {

    private final APIRequestContext request;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ToolUnitsClient(APIRequestContext request) {
        this.request = request;
    }

    /**
     * GET /materials-financings/tools/tool-units/search
     * ?locationsIds={locationId}&page=0&size=10&sort=updatedAt,desc
     */
    public APIResponse searchToolUnitsByLocation(String locationId) {
        String url = "/materials-financings/tools/tool-units/search"
                + "?locationsIds=" + locationId
                + "&page=0&size=10&sort=updatedAt,desc";
        return request.get(url);
    }

    public JsonNode parseResponse(APIResponse response) throws IOException {
        return objectMapper.readTree(response.text());
    }

    /**
     * Повертає перший tool unit із content[], або null, якщо пусто.
     */
    public JsonNode getFirstToolUnit(JsonNode root) {
        JsonNode content = root.get("content");
        if (content == null || !content.isArray() || content.size() == 0) {
            return null;
        }
        return content.get(0);
    }

    public String extractToolUnitId(JsonNode toolUnitNode) {
        if (toolUnitNode == null) {
            return null;
        }
        JsonNode idNode = toolUnitNode.get("id");
        return (idNode == null || idNode.isNull()) ? null : idNode.asText();
    }
}
