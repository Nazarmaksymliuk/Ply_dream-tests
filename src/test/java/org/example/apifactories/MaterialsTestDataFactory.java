package org.example.apifactories;

import java.util.*;

public class MaterialsTestDataFactory {

    private MaterialsTestDataFactory() {
        // utility class
    }

    // ✅ EXISTING (DO NOT TOUCH)
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

    // ✅ EXISTING (DO NOT TOUCH)
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

    // ---------------------------------------------------------------------
    // ✅ NEW: CREATE MATERIAL IN LOCATION (POST /materials-financings/v2/create-material-item)
    // ---------------------------------------------------------------------
    public static Map<String, Object> buildCreateMaterialInLocationRequest(
            String namePrefix,
            String itemNumberPrefix,
            String locationId
    ) {
        long ts = System.currentTimeMillis();

        String materialName = namePrefix + ts;
        String itemNumber = itemNumberPrefix + ts;

        // ---- material ----
        Map<String, Object> material = new HashMap<>();
        material.put("active", true);
        material.put("name", materialName);
        material.put("description", "Created via CRUD API test (in location)");
        material.put("itemNumber", itemNumber);

        material.put("brand", "API-CRUD-Brand");
        material.put("manufacturer", "API-CRUD-Manufacturer");

        material.put("leadTime", "EIGHTEEN_WEEKS");
        material.put("businessMaterial", true);
        material.put("creationSource", "PLY");
        material.put("isSerialized", false);

        Map<String, Object> measurementUnit = new HashMap<>();
        measurementUnit.put("name", "Each");
        measurementUnit.put("abbreviation", "EA");
        measurementUnit.put("creationSource", "PLY");
        material.put("measurementUnit", measurementUnit);

        // ---- location + materialDetails ----
        Map<String, Object> location = new HashMap<>();
        location.put("id", locationId);

        Map<String, Object> materialDetails = new HashMap<>();
        materialDetails.put("quantity", 1);
        materialDetails.put("lowAmount", 1);
        materialDetails.put("criticalAmount", 0);

        String barcode = String.valueOf(10000000000L + new Random().nextInt(900000000));
        materialDetails.put("barcodes", List.of(barcode));

        materialDetails.put("isReplenishable", true);
        materialDetails.put("syncable", true);
        materialDetails.put("expense", false);
        materialDetails.put("taxable", false);

        Map<String, Object> mdl = new HashMap<>();
        mdl.put("location", location);
        mdl.put("materialDetails", materialDetails);

        // ---- variation ----
        Map<String, Object> variation = new HashMap<>();
        variation.put("name", "Single");
        variation.put("description", "Single material item.");
        variation.put("active", true);
        variation.put("costForClient", 0.0);
        variation.put("costForBusiness", 0.0);
        variation.put("defaultLowAmount", 1);
        variation.put("defaultCriticalAmount", 0);
        variation.put("materialDetailsWithLocations", List.of(mdl));

        // ---- root ----
        Map<String, Object> body = new HashMap<>();

        // ✅ IMPORTANT: some validators read name from root (Material Name)
        body.put("name", materialName);
        body.put("itemNumber", itemNumber);

        body.put("material", material);
        body.put("materialVariations", List.of(variation));
        body.put("materialTags", Collections.emptyList());

        return body;
    }


    // ---------------------------------------------------------------------
    // ✅ NEW: UPDATE MATERIAL IN LOCATION
    // PUT /materials-financings/materials/{materialId}
    // ---------------------------------------------------------------------
    public static Map<String, Object> buildUpdateMaterialInLocationRequest(
            String materialId,
            String namePrefix,
            String itemNumberPrefix,
            String locationId
    ) {
        long ts = System.currentTimeMillis();

        String newName = namePrefix + ts;
        String newItemNumber = itemNumberPrefix + ts;

        // ---- material (update core fields) ----
        Map<String, Object> material = new HashMap<>();
        material.put("id", materialId);
        material.put("active", true);
        material.put("name", newName);
        material.put("description", "Updated via CRUD API test (in location)");
        material.put("itemNumber", newItemNumber);

        material.put("brand", "API-CRUD-Brand-Updated");
        material.put("manufacturer", "API-CRUD-Manufacturer-Updated");

        material.put("leadTime", "EIGHTEEN_WEEKS");
        material.put("businessMaterial", true);
        material.put("creationSource", "PLY");
        material.put("isSerialized", false);

        Map<String, Object> measurementUnit = new HashMap<>();
        measurementUnit.put("name", "Each");
        measurementUnit.put("abbreviation", "EA");
        measurementUnit.put("creationSource", "PLY");
        material.put("measurementUnit", measurementUnit);

        // ---- location block (optional inventory update) ----
        Map<String, Object> location = new HashMap<>();
        location.put("id", locationId);

        Map<String, Object> materialDetails = new HashMap<>();
        materialDetails.put("quantity", 2);
        materialDetails.put("lowAmount", 2);
        materialDetails.put("criticalAmount", 1);
        materialDetails.put("syncable", true);

        Map<String, Object> mdl = new HashMap<>();
        mdl.put("location", location);
        mdl.put("materialDetails", materialDetails);

        Map<String, Object> variation = new HashMap<>();
        variation.put("name", "Single");
        variation.put("description", "Single material item. (updated)");
        variation.put("active", true);
        variation.put("materialDetailsWithLocations", List.of(mdl));

        // ---- root ----
        Map<String, Object> body = new HashMap<>();

        // ✅ IMPORTANT: validator expects root-level name (same as create)
        body.put("name", newName);
        body.put("itemNumber", newItemNumber);

        body.put("material", material);
        body.put("materialVariations", List.of(variation));

        return body;
    }

}
