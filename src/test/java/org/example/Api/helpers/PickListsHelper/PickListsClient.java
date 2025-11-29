package org.example.Api.helpers.PickListsHelper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;

import java.io.IOException;

public class PickListsClient {

    private final APIRequestContext request;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PickListsClient(APIRequestContext request) {
        this.request = request;
    }

    public APIResponse createPickList(Object body) {
        return request.post(
                "/pick-lists",
                RequestOptions.create().setData(body)
        );
    }

    public APIResponse updatePickList(String id, Object body) {
        String url = "/pick-lists/" + id;
        return request.put(url, RequestOptions.create().setData(body));
    }

    public APIResponse deletePickListById(String id) {
        String url = "/pick-lists/" + id;
        return request.delete(url);
    }

    public JsonNode parsePickList(APIResponse response) throws IOException {
        return objectMapper.readTree(response.text());
    }

    public String extractPickListId(APIResponse response) throws IOException {
        JsonNode root = objectMapper.readTree(response.text());
        JsonNode idNode = root.get("id");
        if (idNode == null || idNode.isNull()) {
            return null;
        }
        return idNode.asText();
    }
}
