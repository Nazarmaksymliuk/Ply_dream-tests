package org.example.Api.helpers.LocationConsumables;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;

import java.io.IOException;

public class LocationConsumablesClient {

    private final APIRequestContext request;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LocationConsumablesClient(APIRequestContext request) {
        this.request = request;
    }

    /**
     * GET /locations/{locationId}/consumables?page=0&size=10
     */
    public APIResponse getConsumablesInLocation(String locationId) {
        String url = "/locations/" + locationId + "/consumables?page=0&size=10";
        return request.get(url);
    }

    public JsonNode parseResponse(APIResponse response) throws IOException {
        return objectMapper.readTree(response.text());
    }

    /**
     * Повертаємо перший елемент з content[0]
     */
    public JsonNode getFirstConsumable(JsonNode root) {
        if (root == null) {
            return null;
        }
        JsonNode content = root.get("content");
        if (content == null || !content.isArray() || content.size() == 0) {
            return null;
        }
        return content.get(0);
    }

    /**
     * Витягуємо id консюмибл юніта з ноди.
     * ⚠️ Якщо в респонсі інша структура (наприклад "consumableUnit": { "id": ... }),
     * просто підправиш цей метод під свій JSON.
     */
    public String extractConsumableUnitId(JsonNode consumableNode) {
        if (consumableNode == null) {
            return null;
        }

        // варіант 1: id прямо на верхньому рівні
        JsonNode idNode = consumableNode.get("id");
        if (idNode != null && !idNode.isNull()) {
            return idNode.asText();
        }

        // варіант 2: якщо є вкладений "consumableUnit": { "id": ... }
        JsonNode cu = consumableNode.get("consumableUnit");
        if (cu != null && !cu.isNull()) {
            JsonNode cuId = cu.get("id");
            if (cuId != null && !cuId.isNull()) {
                return cuId.asText();
            }
        }

        return null;
    }
}
