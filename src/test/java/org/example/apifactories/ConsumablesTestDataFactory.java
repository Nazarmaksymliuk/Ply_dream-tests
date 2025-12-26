package org.example.apifactories;

import java.util.*;

public class ConsumablesTestDataFactory {

    private ConsumablesTestDataFactory() {
        // utility class
    }

    public static Map<String, Object> buildCreateConsumableBody(
            String namePrefix,
            String itemNumberPrefix,
            String tagPrefix,
            String measurementUnitId,
            String measurementUnitName,
            String measurementUnitAbbreviation
    ) {
        Map<String, Object> body = new HashMap<>();
        long ts = System.currentTimeMillis();

        body.put("name", namePrefix + ts);
        body.put("description", "Created via API E2E consumable test");
        body.put("itemNumber", itemNumberPrefix + ts);
        body.put("costForBusiness", 10.5);

        Map<String, Object> measurementUnit = new HashMap<>();
        measurementUnit.put("id", measurementUnitId);
        measurementUnit.put("name", measurementUnitName);
        measurementUnit.put("abbreviation", measurementUnitAbbreviation);
        body.put("measurementUnit", measurementUnit);

        Map<String, Object> tag = new HashMap<>();
        tag.put("name", tagPrefix + ts);
        body.put("tags", List.of(tag));

        body.put("consumableUnits", Collections.emptyList());

        return body;
    }

    public static Map<String, Object> buildUpdateConsumableBody(
            String id,
            String namePrefix,
            String itemNumberPrefix,
            String tagPrefix,
            String measurementUnitId,
            String measurementUnitName,
            String measurementUnitAbbreviation
    ) {
        Map<String, Object> body = new HashMap<>();
        long ts = System.currentTimeMillis();

        body.put("id", id);
        body.put("name", namePrefix + ts);
        body.put("description", "Updated via API E2E consumable test");
        body.put("itemNumber", itemNumberPrefix + ts);
        body.put("costForBusiness", 20.75);

        Map<String, Object> measurementUnit = new HashMap<>();
        measurementUnit.put("id", measurementUnitId);
        measurementUnit.put("name", measurementUnitName);
        measurementUnit.put("abbreviation", measurementUnitAbbreviation);
        body.put("measurementUnit", measurementUnit);

        Map<String, Object> tag = new HashMap<>();
        tag.put("name", tagPrefix + ts);
        body.put("tags", List.of(tag));

        return body;
    }

    public static Map<String, Object> buildPartialUpdateConsumableBody(String id) {
        Map<String, Object> body = new HashMap<>();
        long ts = System.currentTimeMillis();

        body.put("id", id);
        body.put("description", "Partially updated via PATCH at " + ts);
        body.put("costForBusiness", 30.0);

        return body;
    }
}
