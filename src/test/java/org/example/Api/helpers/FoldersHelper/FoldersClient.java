package org.example.Api.helpers.FoldersHelper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;

import java.io.IOException;
import java.util.Map;

public class FoldersClient {

    private final APIRequestContext request;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FoldersClient(APIRequestContext request) {
        this.request = request;
    }

    // POST /materials-financings/catalogs/folders
    public APIResponse createFolder(Map<String, Object> body) {
        String url = "/materials-financings/catalogs/folders";
        return request.post(url, RequestOptions.create().setData(body));
    }

    // PUT /materials-financings/catalogs/folders/{folderId}
    public APIResponse updateFolder(String folderId, Map<String, Object> body) {
        String url = "/materials-financings/catalogs/folders/" + folderId;
        return request.put(url, RequestOptions.create().setData(body));
    }

    // DELETE /materials-financings/catalogs/folders/{folderId}?transferFolder=&newParentId=
    public APIResponse deleteFolder(String folderId, Boolean transferFolder, String newParentId) {
        StringBuilder url = new StringBuilder("/materials-financings/catalogs/folders/" + folderId);

        boolean hasQuery = false;
        if (transferFolder != null) {
            url.append(hasQuery ? "&" : "?")
                    .append("transferFolder=").append(transferFolder);
            hasQuery = true;
        }
        if (newParentId != null && !newParentId.isEmpty()) {
            url.append(hasQuery ? "&" : "?")
                    .append("newParentId=").append(newParentId);
        }

        return request.delete(url.toString());
    }

    public JsonNode parseFolder(APIResponse response) throws IOException {
        return objectMapper.readTree(response.text());
    }

    public String extractFolderId(APIResponse response) throws IOException {
        JsonNode root = parseFolder(response);
        JsonNode idNode = root.get("id");
        return (idNode == null || idNode.isNull()) ? null : idNode.asText();
    }
}
