package org.example.Api.helpers.MeasurementUnits;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;

import java.io.IOException;

public class MeasurementUnitsClient {

    private final APIRequestContext request;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MeasurementUnitsClient(APIRequestContext request) {
        this.request = request;
    }

    // GET /materials/measurement-units
    public APIResponse getMeasurementUnits() {
        return request.get("/materials/measurement-units");
    }

    public JsonNode parseResponse(APIResponse response) throws IOException {
        return objectMapper.readTree(response.text());
    }

    /**
     * Шукаємо юніт за abbreviation або name (case-insensitive).
     */
    public JsonNode findUnit(JsonNode root, String abbreviation, String name) {
        JsonNode content = root.get("content");
        if (content == null || !content.isArray()) {
            return null;
        }

        for (JsonNode unit : content) {
            String abbr = unit.hasNonNull("abbreviation") ? unit.get("abbreviation").asText() : null;
            String nm = unit.hasNonNull("name") ? unit.get("name").asText() : null;

            boolean abbrMatches = (abbr != null && abbreviation != null &&
                    abbr.equalsIgnoreCase(abbreviation));
            boolean nameMatches = (nm != null && name != null &&
                    nm.equalsIgnoreCase(name));

            if (abbrMatches || nameMatches) {
                return unit;
            }
        }
        return null;
    }

    public String extractId(JsonNode unit) {
        if (unit == null) {
            return null;
        }
        JsonNode idNode = unit.get("id");
        return (idNode == null || idNode.isNull()) ? null : idNode.asText();
    }
}
