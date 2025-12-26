package org.example.apifactories;

import java.util.HashMap;
import java.util.Map;

public class MaterialsTestDataFactory {

    private MaterialsTestDataFactory() {
        // utility class
    }

    public static Map<String, Object> buildCreateMaterialRequest(String namePrefix, String itemNumberPrefix) {
        long ts = System.currentTimeMillis();

        Map<String, Object> body = new HashMap<>();
        body.put("active", true);
        body.put("name", namePrefix + ts);
        body.put("description", "Created via CRUD API test");
        body.put("itemNumber", itemNumberPrefix + ts);
        body.put("brand", "API-CRUD-Brand");
        body.put("manufacturer", "API-CRUD-Manufacturer");

        // body.put("materialType", "TEMPORARY_LINE_ITEM");
        body.put("leadTime", "EIGHTEEN_WEEKS");
        body.put("serialized", false);

        Map<String, Object> measurementUnit = new HashMap<>();
        measurementUnit.put("name", "Each");
        measurementUnit.put("abbreviation", "EA");
        measurementUnit.put("creationSource", "PLY");
        body.put("measurementUnit", measurementUnit);

        return body;
    }

    public static Map<String, Object> buildUpdateMaterialRequest(
            String materialId,
            String namePrefix,
            String itemNumberPrefix
    ) {
        long ts = System.currentTimeMillis();

        Map<String, Object> body = new HashMap<>();
        body.put("id", materialId);
        body.put("active", true);
        body.put("name", namePrefix + ts);
        body.put("description", "Updated via CRUD API test");
        body.put("itemNumber", itemNumberPrefix + ts);
        body.put("brand", "API-CRUD-Brand-Updated");
        body.put("manufacturer", "API-CRUD-Manufacturer-Updated");

        // body.put("materialType", "TEMPORARY_LINE_ITEM");
        body.put("leadTime", "EIGHTEEN_WEEKS");
        body.put("serialized", false);

        Map<String, Object> measurementUnit = new HashMap<>();
        measurementUnit.put("name", "Each");
        measurementUnit.put("abbreviation", "EA");
        measurementUnit.put("creationSource", "PLY");
        body.put("measurementUnit", measurementUnit);

        return body;
    }
}
