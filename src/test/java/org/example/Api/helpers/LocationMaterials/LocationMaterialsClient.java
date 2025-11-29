package org.example.Api.helpers.LocationMaterials;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;

import java.io.IOException;

public class LocationMaterialsClient {

    private final APIRequestContext request;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LocationMaterialsClient(APIRequestContext request) {
        this.request = request;
    }

    /**
     * GET /v2/locations/{locationId}/materials/search?page=0&size=10&sortBy=SORT_BY_UPDATED_AT_DESC
     */
    public APIResponse searchMaterialsInLocation(String locationId) {
        String url = "/v2/locations/" + locationId
                + "/materials/search?page=0&size=10&sortBy=SORT_BY_UPDATED_AT_DESC";
        return request.get(url);
    }

    public JsonNode parseResponse(APIResponse response) throws IOException {
        return objectMapper.readTree(response.text());
    }

    /**
     * Беремо перший матеріал із content[]
     */
    public JsonNode getFirstMaterial(JsonNode root) {
        JsonNode content = root.get("content");
        if (content == null || !content.isArray() || content.size() == 0) {
            return null;
        }
        return content.get(0);
    }

    /**
     * Витягуємо materialVariationId з матеріалу.
     * Точної структури ми не знаємо, тому пробуємо кілька варіантів:
     * - поле materialVariationId
     * - materialVariation.id
     * - materialVariationResponseDto.id
     */
    public String extractMaterialVariationId(JsonNode materialNode) {
        if (materialNode == null) {
            return null;
        }

        // 🔹 те, що реально приходить у твоєму JSON: "variation": { "id": "..." }
        JsonNode variationNode = materialNode.get("variation");
        if (variationNode != null && !variationNode.isNull()) {
            JsonNode idNode = variationNode.get("id");
            if (idNode != null && !idNode.isNull()) {
                return idNode.asText();
            }
        }

        // залишимо запасні варіанти, якщо бек колись зміниться
        JsonNode mvIdNode = materialNode.get("materialVariationId");
        if (mvIdNode != null && !mvIdNode.isNull()) {
            return mvIdNode.asText();
        }

        JsonNode mvNode = materialNode.get("materialVariation");
        if (mvNode != null && !mvNode.isNull()) {
            JsonNode idNode = mvNode.get("id");
            if (idNode != null && !idNode.isNull()) {
                return idNode.asText();
            }
        }

        JsonNode mvResp = materialNode.get("materialVariationResponseDto");
        if (mvResp != null && !mvResp.isNull()) {
            JsonNode idNode = mvResp.get("id");
            if (idNode != null && !idNode.isNull()) {
                return idNode.asText();
            }
        }

        return null;
    }

    public String extractMaterialDetailsId(JsonNode materialNode) {
        if (materialNode == null) {
            return null;
        }
        JsonNode idNode = materialNode.get("id");
        return (idNode == null || idNode.isNull()) ? null : idNode.asText();
    }

}
