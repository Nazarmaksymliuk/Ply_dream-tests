package org.example.apifactories;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class ToolsFinancingTestDataFactory {

    private ToolsFinancingTestDataFactory() {
        // utility class
    }

    public static Map<String, Object> buildCreateToolsFinancingBody(
            String namePrefix,
            String description,
            String mfgPrefix,
            String materialTagId,
            String materialTagName,
            String supplierId,
            String locationId
    ) {
        Map<String, Object> body = new HashMap<>();
        long ts = System.currentTimeMillis();

        body.put("name", namePrefix + ts);
        body.put("description", description);
        body.put("mfg", mfgPrefix + ts);

        Map<String, Object> tag = new HashMap<>();
        tag.put("id", materialTagId);
        if (materialTagName != null) {
            tag.put("name", materialTagName);
        }
        body.put("tags", List.of(tag));

        Map<String, Object> toolUnit = new HashMap<>();
        toolUnit.put("name", "API Tool Unit " + ts);
        toolUnit.put("note", "Created from materials-financings/tools E2E test");

        toolUnit.put("barcodes", List.of("API-BC-" + ts));
        toolUnit.put("jobIds", Collections.emptyList());

        toolUnit.put("locationId", locationId);

        Map<String, Object> locationPosition = new HashMap<>();
        locationPosition.put("aisle", "A1");
        locationPosition.put("bay", "B1");
        locationPosition.put("bin", "BIN1");
        locationPosition.put("level", "L1");
        toolUnit.put("locationPosition", locationPosition);

        toolUnit.put("purchaseCost", 100.0);
        toolUnit.put("purchaseValue", 100.0);

        String purchaseDate = LocalDateTime.now()
                .truncatedTo(ChronoUnit.MICROS)
                .toString();
        toolUnit.put("purchaseDate", purchaseDate);

        toolUnit.put("serialNumber", "SN-" + ts);
        toolUnit.put("status", "AVAILABLE");
        toolUnit.put("supplierId", supplierId);

        body.put("toolUnits", List.of(toolUnit));

        return body;
    }

    public static Map<String, Object> buildUpdateToolsFinancingBody(
            String financingId,
            String toolUnitId,
            String namePrefix,
            String description,
            String mfgPrefix,
            String materialTagId,
            String materialTagName,
            String supplierId,
            String locationId
    ) {
        Map<String, Object> body = new HashMap<>();
        long ts = System.currentTimeMillis();

        body.put("id", financingId);
        body.put("name", namePrefix + ts);
        body.put("description", description);
        body.put("mfg", mfgPrefix + ts);

        Map<String, Object> tag = new HashMap<>();
        tag.put("id", materialTagId);
        if (materialTagName != null) {
            tag.put("name", materialTagName);
        }
        body.put("tags", List.of(tag));

        Map<String, Object> toolUnit = new HashMap<>();
        toolUnit.put("id", toolUnitId);
        toolUnit.put("name", "API Tool Unit UPDATED " + ts);
        toolUnit.put("note", "Updated from materials-financings/tools E2E test");

        toolUnit.put("barcodes", List.of("API-BC-UPDATED-" + ts));
        toolUnit.put("jobIds", Collections.emptyList());

        toolUnit.put("locationId", locationId);

        Map<String, Object> locationPosition = new HashMap<>();
        locationPosition.put("aisle", "A2");
        locationPosition.put("bay", "B2");
        locationPosition.put("bin", "BIN2");
        locationPosition.put("level", "L2");
        toolUnit.put("locationPosition", locationPosition);

        toolUnit.put("purchaseCost", 200.0);
        toolUnit.put("purchaseValue", 200.0);

        String purchaseDate = LocalDateTime.now()
                .truncatedTo(ChronoUnit.MICROS)
                .toString();
        toolUnit.put("purchaseDate", purchaseDate);

        toolUnit.put("serialNumber", "SN-UPDATED-" + ts);
        toolUnit.put("status", "AVAILABLE");
        toolUnit.put("supplierId", supplierId);

        body.put("toolUnits", List.of(toolUnit));

        return body;
    }
}
