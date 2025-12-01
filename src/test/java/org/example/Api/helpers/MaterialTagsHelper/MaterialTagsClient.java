package org.example.Api.helpers.MaterialTagsHelper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;

import java.io.IOException;

public class MaterialTagsClient {

    private final APIRequestContext request;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MaterialTagsClient(APIRequestContext request) {
        this.request = request;
    }

    // GET /materials-financings/materials/material-tags?pageNumber=0&pageSize=20
    public APIResponse getMaterialTags(int pageNumber, int pageSize) {
        String url = String.format(
                "/materials-financings/materials/material-tags?pageNumber=%d&pageSize=%d",
                pageNumber, pageSize
        );
        return request.get(url);
    }

    public JsonNode parseResponse(APIResponse response) throws IOException {
        return objectMapper.readTree(response.text());
    }

    /**
     * Повертає id першого тега:
     *  - якщо root = [ {..}, {..} ] → root[0].id
     *  - якщо root = { "content": [ {..}, .. ] } → content[0].id
     */
    public String extractFirstTagId(JsonNode root) {
        JsonNode first = null;

        if (root.isArray() && root.size() > 0) {
            first = root.get(0);
        } else if (root.has("content") && root.get("content").isArray()
                && root.get("content").size() > 0) {
            first = root.get("content").get(0);
        }

        if (first == null) {
            return null;
        }

        JsonNode idNode = first.get("id");
        return (idNode == null || idNode.isNull()) ? null : idNode.asText();
    }

    public String extractFirstTagName(JsonNode root) {
        JsonNode first = null;

        if (root.isArray() && root.size() > 0) {
            first = root.get(0);
        } else if (root.has("content") && root.get("content").isArray()
                && root.get("content").size() > 0) {
            first = root.get("content").get(0);
        }

        if (first == null) {
            return null;
        }

        JsonNode nameNode = first.get("name");
        return (nameNode == null || nameNode.isNull()) ? null : nameNode.asText();
    }
}
