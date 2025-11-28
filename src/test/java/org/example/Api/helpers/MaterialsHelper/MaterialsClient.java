package org.example.Api.helpers.MaterialsHelper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;

import java.io.IOException;
import java.util.Map;

public class MaterialsClient {

    private final APIRequestContext request;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MaterialsClient(APIRequestContext request) {
        this.request = request;
    }

    // POST /materials-financings/v2/create-material-item
    public APIResponse createMaterial(Map<String, Object> body) {
        return request.post(
                "/materials-financings/v2/create-material-item",
                RequestOptions.create().setData(body)
        );
    }

    // PUT /materials-financings/materials/{materialId}
    public APIResponse updateMaterial(String materialId, Map<String, Object> body) {
        return request.put(
                "/materials-financings/materials/" + materialId,
                RequestOptions.create().setData(body)
        );
    }

    // DELETE /materials-financings/materials/{id}
    public APIResponse deleteMaterial(String materialId) {
        return request.delete(
                "/materials-financings/materials/" + materialId
        );
    }

    // Витягнути вузол "material" з відповіді
    public JsonNode extractMaterialNode(APIResponse response) throws IOException {
        String json = response.text();
        JsonNode root = objectMapper.readTree(json);
        return root.get("material");
    }

    // Витягнути materialId з відповіді
    public String extractMaterialId(APIResponse response) throws IOException {
        JsonNode materialNode = extractMaterialNode(response);
        if (materialNode == null || materialNode.get("id") == null) {
            return null;
        }
        return materialNode.get("id").asText();
    }
}
