package org.example.Api.helpers.ConsumablesHelper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ConsumablesClient {

    private final APIRequestContext request;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ConsumablesClient(APIRequestContext request) {
        this.request = request;
    }

    // POST /consumables/create
    public APIResponse createConsumable(Map<String, Object> body) {
        String url = "/consumables/create";
        return request.post(url, RequestOptions.create().setData(body));
    }

    // PUT /consumables/{id}
    public APIResponse updateConsumable(String id, Map<String, Object> body) {
        String url = "/consumables/" + id;
        return request.put(url, RequestOptions.create().setData(body));
    }

    // PATCH /consumables/{id}
    public APIResponse partialUpdateConsumable(String id, Map<String, Object> body) {
        String url = "/consumables/" + id;
        return request.patch(url, RequestOptions.create().setData(body));
    }

    // DELETE /consumables  (body: { "ids": ["..."] })
    public APIResponse deleteConsumables(List<String> ids) {
        String url = "/consumables";
        Map<String, Object> body = Map.of("ids", ids);
        return request.delete(url, RequestOptions.create().setData(body));
    }

    public JsonNode parseConsumable(APIResponse response) throws IOException {
        return objectMapper.readTree(response.text());
    }

    public String extractConsumableId(APIResponse response) throws IOException {
        JsonNode root = parseConsumable(response);
        JsonNode idNode = root.get("id");
        return (idNode == null || idNode.isNull()) ? null : idNode.asText();
    }
}
