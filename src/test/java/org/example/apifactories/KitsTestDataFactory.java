package org.example.apifactories;

import java.util.*;

public class KitsTestDataFactory {

    private KitsTestDataFactory() {}

    public static Map<String, Object> buildCreateKitBodyWithMaterialToolAndLocation(
            String namePrefix,
            String description,
            double cost,
            String materialVariationId,
            String toolUnitId,
            String locationId
    ) {
        Map<String, Object> body = new HashMap<>();
        long ts = System.currentTimeMillis();

        body.put("name", namePrefix + ts);
        body.put("description", description);
        body.put("cost", cost);

        body.put("barcodes", Collections.emptyList());

        Map<String, Object> kitMaterial = new HashMap<>();
        kitMaterial.put("materialVariationId", materialVariationId);
        kitMaterial.put("quantity", 1);
        kitMaterial.put("cost", cost);
        body.put("kitMaterialVariationRequestDtos", List.of(kitMaterial));

        body.put("toolUnitIds", List.of(toolUnitId));
        body.put("locationIds", List.of(locationId));

        Map<String, Object> locationWithPos = new HashMap<>();
        locationWithPos.put("locationId", locationId);

        Map<String, Object> position = new HashMap<>();
        position.put("aisle", null);
        position.put("bay", null);
        position.put("bin", null);
        position.put("level", null);
        locationWithPos.put("locationPosition", position);

        body.put("locationIdsWithPositions", List.of(locationWithPos));
        body.put("kitTagRequestDtos", Collections.emptyList());

        return body;
    }

    public static Map<String, Object> buildUpdateKitBodyWithSameLinks(
            String namePrefix,
            String description,
            double cost,
            String materialVariationId,
            String toolUnitId,
            String locationId
    ) {
        return buildCreateKitBodyWithMaterialToolAndLocation(
                namePrefix, description, cost, materialVariationId, toolUnitId, locationId
        );
    }
}
