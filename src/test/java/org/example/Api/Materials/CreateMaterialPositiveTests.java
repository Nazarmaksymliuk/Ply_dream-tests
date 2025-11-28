package org.example.Api.Materials;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.APIResponse;
import org.example.Api.helpers.MaterialsHelper.MaterialsClient;
import org.example.BaseAPITestExtension.BaseApiTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateMaterialPositiveTests extends BaseApiTest {

    private MaterialsClient materialsClient;

    @BeforeAll
    void initClient() {
        // ⚠️ ВАЖЛИВО:
        // Тут ми припускаємо, що BaseApiTest вже додає Authorization header (Bearer <token>).
        // Якщо ні — треба буде або:
        // 1) залогінитись тут і створити новий APIRequestContext з Authorization,
        // 2) або додати токен в headers у BaseApiTest.
        materialsClient = new MaterialsClient(apiRequest);
    }

    @Test
    void createMaterial_returns200_andMaterialHasId() throws IOException {
        Map<String, Object> body = buildValidMaterialRequest();

        APIResponse response = materialsClient.createMaterial(body);
        int status = response.status();

        System.out.println("Status: " + status);
        System.out.println("URL: " + response.url());
        System.out.println("Body: " + response.text());

        Assertions.assertEquals(200, status, "Expected 200 OK when creating material");

        JsonNode materialNode = materialsClient.extractMaterialNode(response);
        Assertions.assertNotNull(materialNode, "Response should contain 'material' field");

        JsonNode idNode = materialNode.get("id");
        Assertions.assertNotNull(idNode, "Material 'id' should not be null");
        Assertions.assertFalse(idNode.asText().isEmpty(), "Material 'id' should not be empty");

        // Перевіримо, що імʼя таке, як ми відправили
        String sentName = (String) body.get("name");
        String actualName = materialNode.get("name").asText();
        Assertions.assertEquals(sentName, actualName, "Material name in response should match request");
    }

    private Map<String, Object> buildValidMaterialRequest() {
        Map<String, Object> body = new HashMap<>();

        // basic fields
        body.put("active", true);
        body.put("name", "API Test Material " + System.currentTimeMillis());
        body.put("description", "Created via Playwright API test");
        body.put("itemNumber", "API-" + System.currentTimeMillis());
        body.put("brand", "API-Test-Brand");
        body.put("manufacturer", "API-Test-Manufacturer");

        // enum-like fields із swagger
        body.put("materialType", "TEMPORARY_LINE_ITEM");
        body.put("leadTime", "EIGHTEEN_WEEKS");
        body.put("serialized", false);

        // якщо дозволяє бекенд — можна лишити прості строки
        body.put("materialCategory", "TEST_CATEGORY");
        body.put("materialCategories", List.of("TEST_CATEGORY_1", "TEST_CATEGORY_2"));

        // measurementUnit – без id, тільки опис
        Map<String, Object> measurementUnit = new HashMap<>();
        measurementUnit.put("name", "Each");
        measurementUnit.put("abbreviation", "EA");
        measurementUnit.put("creationSource", "PLY");
        body.put("measurementUnit", measurementUnit);

        // ❌ вирізали materialTags
        // ❌ вирізали materialCustomFields
        // ❌ вирізали customFields
        // ❌ вирізали materialsCategories з id
        // ❌ вирізали materialVariations з id

        return body;
    }

}
