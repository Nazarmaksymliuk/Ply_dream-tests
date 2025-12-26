package org.example.apifactories;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PickListsTestDataFactory {

    private PickListsTestDataFactory() {
        // utility class
    }

    public static Map<String, Object> buildCreatePickListBody(
            String namePrefix,
            String fromLocationId,
            String toLocationId,
            String materialDetailsFromLocationId,
            String consumableUnitFromLocationId
    ) {
        Map<String, Object> body = new HashMap<>();
        long ts = System.currentTimeMillis();

        body.put("name", namePrefix + ts);
        body.put("note", "Created via API E2E pick list test");
        body.put("requestDate", LocalDate.now().toString());

        Map<String, Object> fromLocation = new HashMap<>();
        fromLocation.put("id", fromLocationId);
        body.put("fromLocation", fromLocation);

        Map<String, Object> toLocation = new HashMap<>();
        toLocation.put("id", toLocationId);
        body.put("toLocation", toLocation);

        Map<String, Object> pickListMaterial = new HashMap<>();
        pickListMaterial.put("materialDetailsFromLocationId", materialDetailsFromLocationId);
        pickListMaterial.put("quantity", 1);
        pickListMaterial.put("note", "API material for pick list");
        pickListMaterial.put("syncable", true);
        body.put("pickListMaterials", List.of(pickListMaterial));

        Map<String, Object> pickListConsumable = new HashMap<>();
        pickListConsumable.put("consumableUnitFromLocationId", consumableUnitFromLocationId);
        pickListConsumable.put("quantity", 1);
        pickListConsumable.put("note", "API consumable for pick list");
        body.put("pickListConsumableRequestDtos", List.of(pickListConsumable));

        return body;
    }

    public static Map<String, Object> buildUpdatePickListBody(
            String pickListId,
            String namePrefix,
            String toLocationId
    ) {
        Map<String, Object> body = new HashMap<>();
        long ts = System.currentTimeMillis();

        body.put("id", pickListId);
        body.put("name", namePrefix + ts);
        body.put("note", "Updated via API E2E pick list test");
        body.put("requestDate", LocalDate.now().toString());
        body.put("status", "ARCHIVED"); // як у swagger прикладі

        Map<String, Object> toLocation = new HashMap<>();
        toLocation.put("id", toLocationId);
        body.put("toLocation", toLocation);

        return body;
    }
}
