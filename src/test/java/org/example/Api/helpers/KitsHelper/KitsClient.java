package org.example.Api.helpers.KitsHelper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class KitsClient {

    private final APIRequestContext request;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public KitsClient(APIRequestContext request) {
        this.request = request;
    }

    // POST /kits/create
    public APIResponse createKit(Map<String, Object> body) {
        return request.post("/kits/create", RequestOptions.create().setData(body));
    }

    // PUT /kits/{kitId}/update
    public APIResponse updateKit(String kitId, Map<String, Object> body) {
        String url = "/kits/" + kitId + "/update";
        return request.put(url, RequestOptions.create().setData(body));
    }

    // DELETE /kits/delete  (body: { "ids": [kitId] })
    public APIResponse deleteKits(List<String> ids) {
        Map<String, Object> body = Map.of("ids", ids);
        return request.delete("/kits/delete", RequestOptions.create().setData(body));
    }

    // ---------- helpers ----------

    public String extractKitId(APIResponse response) throws IOException {
        String json = response.text();
        JsonNode root = objectMapper.readTree(json);
        JsonNode idNode = root.get("id");
        return (idNode == null || idNode.isNull()) ? null : idNode.asText();
    }

    public JsonNode parseKit(APIResponse response) throws IOException {
        return objectMapper.readTree(response.text());
    }
}
