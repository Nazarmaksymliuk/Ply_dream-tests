package org.example.Api.helpers.MaterialDetails;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;

import java.io.IOException;

public class MaterialDetailsClient {

    private final APIRequestContext request;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MaterialDetailsClient(APIRequestContext request) {
        this.request = request;
    }

    /**
     * GET /locations/{locationId}/material-details
     * без зайвих query-параметрів
     */
    public APIResponse getMaterialDetailsForLocation(String locationId) {
        String url = "/locations/" + locationId + "/material-details";
        return request.get(url);
    }

    public JsonNode parseResponse(APIResponse response) throws IOException {
        return objectMapper.readTree(response.text());
    }

    public JsonNode getFirstMaterialDetails(JsonNode root) {
        JsonNode content = root.get("content");
        if (content == null || !content.isArray() || content.size() == 0) {
            return null;
        }
        return content.get(0);
    }

    public String extractMaterialVariationId(JsonNode materialDetailsNode) {
        if (materialDetailsNode == null) {
            return null;
        }

        // варіант 1: materialVariation.id
        JsonNode mvNode = materialDetailsNode.get("materialVariation");
        if (mvNode != null && !mvNode.isNull()) {
            JsonNode idNode = mvNode.get("id");
            if (idNode != null && !idNode.isNull()) {
                return idNode.asText();
            }
        }

        // варіант 2: materialVariationResponseDto.id
        JsonNode mvResp = materialDetailsNode.get("materialVariationResponseDto");
        if (mvResp != null && !mvResp.isNull()) {
            JsonNode idNode = mvResp.get("id");
            if (idNode != null && !idNode.isNull()) {
                return idNode.asText();
            }
        }

        return null;
    }
}
