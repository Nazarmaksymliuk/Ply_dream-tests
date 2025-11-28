package org.example.Api.helpers.LocationsHelper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;

import java.io.IOException;
import java.util.Map;

public class LocationsClient {

    private final APIRequestContext request;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LocationsClient(APIRequestContext request) {
        this.request = request;
    }

    // POST /locations?pinned={true|false}
    public APIResponse createLocation(Map<String, Object> body, boolean pinned) {
        String url = "/locations?pinned=" + pinned;
        return request.post(
                url,
                RequestOptions.create().setData(body)
        );
    }

    // PUT /locations/{id}?pinned={true|false}
    public APIResponse updateLocation(String id, Map<String, Object> body, boolean pinned) {
        String url = "/locations/" + id + "?pinned=" + pinned;
        return request.put(
                url,
                RequestOptions.create().setData(body)
        );
    }

    // DELETE /locations/{id}[?toLocationId=...]
    // body { "text": "reason" }
    public APIResponse deleteLocation(String id, String toLocationId, String text) {
        String url = "/locations/" + id;
        if (toLocationId != null && !toLocationId.isEmpty()) {
            url += "?toLocationId=" + toLocationId;
        }

        Map<String, Object> body = Map.of("text", text);

        return request.delete(
                url,
                RequestOptions.create().setData(body)
        );
    }

    public String extractLocationId(APIResponse response) throws IOException {
        String json = response.text();
        JsonNode root = objectMapper.readTree(json);
        JsonNode idNode = root.get("id");
        if (idNode == null || idNode.isNull()) {
            return null;
        }
        return idNode.asText();
    }

    public JsonNode parseLocation(APIResponse response) throws IOException {
        String json = response.text();
        return objectMapper.readTree(json);
    }
}
