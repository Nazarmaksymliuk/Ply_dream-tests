package org.example.Api.Materials;

import com.microsoft.playwright.APIResponse;
import org.example.Api.helpers.MaterialsHelper.MaterialsClient;
import org.example.BaseAPITestExtension.BaseApiTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class CreateMaterialNegativeTests extends BaseApiTest {

    private MaterialsClient materialsClient;

    @BeforeAll
    void initClient() {
        materialsClient = new MaterialsClient(apiRequest);
    }

    @Test
    void createMaterial_withoutName_returns4xx() {
        Map<String, Object> body = new HashMap<>();

        body.put("active", true);
        body.put("description", "No name field");
        body.put("itemNumber", "NO-NAME-" + System.currentTimeMillis());
        body.put("materialType", "TEMPORARY_LINE_ITEM");
        body.put("serialized", false);

        Map<String, Object> measurementUnit = new HashMap<>();
        measurementUnit.put("name", "Each");
        measurementUnit.put("abbreviation", "EA");
        measurementUnit.put("creationSource", "PLY");
        body.put("measurementUnit", measurementUnit);

        APIResponse response = materialsClient.createMaterial(body);
        int status = response.status();

        Assertions.assertTrue(
                status == 400 || status == 401 || status == 404 || status == 422,
                "Expected 4xx for missing name, but got: " + status
        );
    }

    @Test
    void createMaterial_withInvalidLeadTime_returns4xx() {
        Map<String, Object> body = new HashMap<>();

        body.put("active", true);
        body.put("name", "API Material invalid leadTime " + System.currentTimeMillis());
        body.put("description", "Invalid leadTime value");
        body.put("itemNumber", "BAD-LT-" + System.currentTimeMillis());
        body.put("materialType", "TEMPORARY_LINE_ITEM");
        body.put("leadTime", "INVALID_LEAD_TIME"); // ❌ невалідне enum-значення
        body.put("serialized", false);

        Map<String, Object> measurementUnit = new HashMap<>();
        measurementUnit.put("name", "Each");
        measurementUnit.put("abbreviation", "EA");
        measurementUnit.put("creationSource", "PLY");
        body.put("measurementUnit", measurementUnit);

        APIResponse response = materialsClient.createMaterial(body);
        int status = response.status();

        Assertions.assertTrue(
                status == 400 || status == 401 || status == 404 || status == 422,
                "Expected 4xx for invalid leadTime, but got: " + status
        );
    }

    @Test
    void createMaterial_withInvalidMaterialType_returns4xx() {
        Map<String, Object> body = new HashMap<>();

        body.put("active", true);
        body.put("name", "API Material invalid type " + System.currentTimeMillis());
        body.put("description", "Invalid materialType");
        body.put("itemNumber", "BAD-TYPE-" + System.currentTimeMillis());
        body.put("materialType", "INVALID_TYPE"); // ❌ невалідний enum
        body.put("serialized", false);

        Map<String, Object> measurementUnit = new HashMap<>();
        measurementUnit.put("name", "Each");
        measurementUnit.put("abbreviation", "EA");
        measurementUnit.put("creationSource", "PLY");
        body.put("measurementUnit", measurementUnit);

        APIResponse response = materialsClient.createMaterial(body);
        int status = response.status();

        Assertions.assertTrue(
                status == 400 || status == 401 || status == 404 || status == 422,
                "Expected 4xx for invalid materialType, but got: " + status
        );
    }
}
