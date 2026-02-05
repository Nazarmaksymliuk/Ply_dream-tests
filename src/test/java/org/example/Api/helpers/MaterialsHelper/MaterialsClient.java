package org.example.Api.helpers.MaterialsHelper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

    // ✅ NEW: POST /locations/materials?isReconciled=false  (attach material to location)
    public APIResponse attachMaterialToLocation(Map<String, Object> body) {
        return request.post(
                "/locations/materials",
                RequestOptions.create()
                        .setQueryParam("isReconciled", "false")
                        .setData(body)
        );
    }

    // ✅ NEW: GET /materials-financings/materials/{id} (verify attachment)
    public APIResponse getMaterial(String materialId) {
        return request.get("/materials-financings/materials/" + materialId);
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
        return request.delete("/materials-financings/materials/" + materialId);
    }

    public JsonNode extractMaterialNode(APIResponse response) throws IOException {
        JsonNode root = objectMapper.readTree(response.text());
        return root.get("material");
    }

    public String extractMaterialId(APIResponse response) throws IOException {
        JsonNode materialNode = extractMaterialNode(response);
        if (materialNode == null || materialNode.get("id") == null) return null;
        return materialNode.get("id").asText();
    }

    // ✅ NEW: extract first variationId from create response
    public String extractFirstVariationId(APIResponse response) throws IOException {
        JsonNode root = objectMapper.readTree(response.text());
        JsonNode variations = root.path("materialVariations");
        if (!variations.isArray() || variations.size() == 0) return null;
        JsonNode idNode = variations.get(0).get("id");
        return idNode == null ? null : idNode.asText();
    }

    public APIResponse checkDuplicates(Map<String, Object> body) {
        return request.post(
                "/materials-financings/duplicates",
                RequestOptions.create().setData(body)
        );
    }

    public List<String> extractErrorMessages(APIResponse response) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.text());

        List<String> messages = new ArrayList<>();
        JsonNode msgNode = root.get("message");

        if (msgNode == null || msgNode.isNull()) return messages;

        if (msgNode.isArray()) {
            for (JsonNode n : msgNode) {
                if (n != null && n.isTextual()) messages.add(n.asText());
            }
        } else if (msgNode.isTextual()) {
            messages.add(msgNode.asText());
        }

        return messages;
    }


}
