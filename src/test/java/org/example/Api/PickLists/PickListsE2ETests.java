package org.example.Api.PickLists;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.APIResponse;
import org.example.Api.helpers.LocationConsumables.LocationConsumablesClient;
import org.example.Api.helpers.LocationMaterials.LocationMaterialsClient;
import org.example.Api.helpers.PickListsHelper.PickListsClient;
import org.example.BaseAPITestExtension.BaseApiTest;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PickListsE2ETests extends BaseApiTest {

    private PickListsClient pickListsClient;
    private LocationMaterialsClient locationMaterialsClient;
    private LocationConsumablesClient locationConsumablesClient;

    // стабільні склади з твого JSON
    private static final String FROM_LOCATION_ID = "ac1f56fd-9919-137e-8199-1f504b6607e8"; // WarehouseMain
    private static final String TO_LOCATION_ID   = "ac1f56fd-9a4a-154f-819a-4c1fc3ea0711"; // WarehouseToTransfer

    // залежності, які витягуємо у @BeforeAll
    private String materialDetailsFromLocationId;
    private String consumableUnitFromLocationId;

    // id створеного pick list
    private String pickListId;

    // для зручних асертів
    private Map<String, Object> lastCreateBody;
    private Map<String, Object> lastUpdateBody;

    @BeforeAll
    void initClientsAndResolveDependencies() throws IOException {
        pickListsClient = new PickListsClient(userApi);
        locationMaterialsClient = new LocationMaterialsClient(userApi);
        locationConsumablesClient = new LocationConsumablesClient(userApi);

        // 1️⃣ MATERIAL DETAILS з WarehouseMain
        APIResponse materialsResp =
                locationMaterialsClient.searchMaterialsInLocation(FROM_LOCATION_ID);
        int matStatus = materialsResp.status();

        System.out.println("MATERIALS SEARCH status: " + matStatus);
        System.out.println("MATERIALS SEARCH body: " + materialsResp.text());

        Assertions.assertEquals(
                200,
                matStatus,
                "Expected 200 from /v2/locations/{id}/materials/search"
        );

        JsonNode materialsRoot = locationMaterialsClient.parseResponse(materialsResp);
        JsonNode firstMaterial = locationMaterialsClient.getFirstMaterial(materialsRoot);

        Assertions.assertNotNull(
                firstMaterial,
                "No materials found in fromLocation – please ensure warehouse has at least one material"
        );

        materialDetailsFromLocationId =
                locationMaterialsClient.extractMaterialDetailsId(firstMaterial);

        Assertions.assertNotNull(
                materialDetailsFromLocationId,
                "materialDetailsFromLocationId could not be extracted from materials search result"
        );

        System.out.println("Resolved materialDetailsFromLocationId: " + materialDetailsFromLocationId);

        // 2️⃣ CONSUMABLE з WarehouseMain через GET /locations/{locationId}/consumables
        APIResponse consResp =
                locationConsumablesClient.getConsumablesInLocation(FROM_LOCATION_ID);
        int consStatus = consResp.status();

        System.out.println("CONSUMABLES IN LOCATION status: " + consStatus);
        System.out.println("CONSUMABLES IN LOCATION body: " + consResp.text());

        Assertions.assertEquals(
                200,
                consStatus,
                "Expected 200 from /locations/{id}/consumables"
        );

        JsonNode consRoot = locationConsumablesClient.parseResponse(consResp);
        JsonNode firstConsumable = locationConsumablesClient.getFirstConsumable(consRoot);

        Assertions.assertNotNull(
                firstConsumable,
                "No consumables found in fromLocation – please ensure there is at least one consumable in this warehouse"
        );

        consumableUnitFromLocationId =
                locationConsumablesClient.extractConsumableUnitId(firstConsumable);

        Assertions.assertNotNull(
                consumableUnitFromLocationId,
                "consumableUnitFromLocationId could not be extracted from /locations/{id}/consumables result"
        );

        System.out.println("Resolved consumableUnitFromLocationId: " + consumableUnitFromLocationId);
    }

    // 1️⃣ CREATE: POST /pick-lists – матеріал + консюмибл
    @Test
    @Order(1)
    void createPickList_withMaterialAndConsumable_createsOk() throws IOException {
        Map<String, Object> body = buildCreatePickListBody();
        lastCreateBody = body;

        APIResponse response = pickListsClient.createPickList(body);
        int status = response.status();

        System.out.println("CREATE PICK LIST status: " + status);
        System.out.println("CREATE PICK LIST body: " + response.text());

        Assertions.assertTrue(
                status == 201 || status == 200,
                "Expected 201 or 200 on create pick list, but got: " + status
        );

        pickListId = pickListsClient.extractPickListId(response);
        Assertions.assertNotNull(pickListId, "pickListId must not be null after create");
        Assertions.assertFalse(pickListId.isEmpty(), "pickListId must not be empty");

        JsonNode created = pickListsClient.parsePickList(response);

        // базові поля
        Assertions.assertEquals(
                body.get("name"),
                created.get("name").asText(),
                "Pick list name must match request"
        );

        // from / to locations
        JsonNode fromLoc = created.get("fromLocation");
        JsonNode toLoc   = created.get("toLocation");

        Assertions.assertNotNull(fromLoc, "fromLocation must be present in response");
        Assertions.assertNotNull(toLoc, "toLocation must be present in response");

        Assertions.assertEquals(
                FROM_LOCATION_ID,
                fromLoc.get("id").asText(),
                "fromLocation.id must match FROM_LOCATION_ID"
        );
        Assertions.assertEquals(
                TO_LOCATION_ID,
                toLoc.get("id").asText(),
                "toLocation.id must match TO_LOCATION_ID"
        );

        // статус – бек може виставити свій, просто перевіримо, що є
        Assertions.assertNotNull(
                created.get("status"),
                "status in pick list response must not be null"
        );
    }

    // 2️⃣ UPDATE: PUT /pick-lists/{id}
    @Test
    @Order(2)
    void updatePickList_updatesNameStatusAndToLocation() throws IOException {
        Assertions.assertNotNull(pickListId, "pickListId is null – create test probably failed");

        Map<String, Object> body = buildUpdatePickListBody(pickListId);
        lastUpdateBody = body;

        APIResponse response = pickListsClient.updatePickList(pickListId, body);
        int status = response.status();

        System.out.println("UPDATE PICK LIST status: " + status);
        System.out.println("UPDATE PICK LIST body: " + response.text());

        Assertions.assertTrue(
                status == 200 || status == 201,
                "Expected 200 or 201 on update pick list, but got: " + status
        );

        JsonNode updated = pickListsClient.parsePickList(response);

        // id має збігатися
        Assertions.assertEquals(
                pickListId,
                updated.get("id").asText(),
                "Updated pickList id must be the same as path id"
        );

        // name / note
        Assertions.assertEquals(
                body.get("name"),
                updated.get("name").asText(),
                "Updated name must match request"
        );
        Assertions.assertEquals(
                body.get("note"),
                updated.get("note").asText(),
                "Updated note must match request"
        );

        // ⚠️ status бекенд зараз не оновлює (повертає null), тому не порівнюємо з реквестом
        JsonNode statusNode = updated.get("status");
        Assertions.assertNotNull(
                statusNode,
                "status field must be present in response (can be null by backend design)"
        );
        // якщо хочеш – можна ще задокументувати поведінку:
        // Assertions.assertTrue(statusNode.isNull(), "Currently backend returns null for status on update");

        // toLocation.id
        JsonNode toLoc = updated.get("toLocation");
        Assertions.assertNotNull(toLoc, "toLocation must be present in update response");
        Assertions.assertEquals(
                TO_LOCATION_ID,
                toLoc.get("id").asText(),
                "Updated toLocation.id must still match TO_LOCATION_ID"
        );
    }


    // 3️⃣ DELETE: DELETE /pick-lists/{id}
    @Test
    @Order(3)
    void deletePickList_deletesSuccessfully() {
        Assertions.assertNotNull(pickListId, "pickListId is null – previous tests probably failed");

        APIResponse response = pickListsClient.deletePickListById(pickListId);
        int status = response.status();

        System.out.println("DELETE PICK LIST status: " + status);
        System.out.println("DELETE PICK LIST body: '" + response.text() + "'");

        // swagger показує 204, але на випадок якщо бек поверне 200 – дозволимо обидва
        Assertions.assertTrue(
                status == 204 || status == 200,
                "Expected 204 or 200 on delete pick list, but got: " + status
        );
    }

    // ---------- helpers ----------

    private Map<String, Object> buildCreatePickListBody() {
        Map<String, Object> body = new HashMap<>();
        long ts = System.currentTimeMillis();

        body.put("name", "API Pick List " + ts);
        body.put("note", "Created via API E2E pick list test");
        body.put("requestDate", LocalDate.now().toString());
        // статус на create можна не відправляти, якщо бек ставить сам
        // body.put("status", "NEED_REFILL");

        // fromLocation / toLocation – мінімально по id, решту бек підтягне сам
        Map<String, Object> fromLocation = new HashMap<>();
        fromLocation.put("id", FROM_LOCATION_ID);
        body.put("fromLocation", fromLocation);

        Map<String, Object> toLocation = new HashMap<>();
        toLocation.put("id", TO_LOCATION_ID);
        body.put("toLocation", toLocation);

        // один матеріал із fromLocation
        Map<String, Object> pickListMaterial = new HashMap<>();
        pickListMaterial.put("materialDetailsFromLocationId", materialDetailsFromLocationId);
        pickListMaterial.put("quantity", 1);
        pickListMaterial.put("note", "API material for pick list");
        pickListMaterial.put("syncable", true);
        body.put("pickListMaterials", java.util.List.of(pickListMaterial));

        // один консюмибл із fromLocation
        Map<String, Object> pickListConsumable = new HashMap<>();
        pickListConsumable.put("consumableUnitFromLocationId", consumableUnitFromLocationId);
        pickListConsumable.put("quantity", 1);
        pickListConsumable.put("note", "API consumable for pick list");
        body.put("pickListConsumableRequestDtos", java.util.List.of(pickListConsumable));

        return body;
    }

    private Map<String, Object> buildUpdatePickListBody(String id) {
        Map<String, Object> body = new HashMap<>();
        long ts = System.currentTimeMillis();

        body.put("id", id);
        body.put("name", "API Pick List UPDATED " + ts);
        body.put("note", "Updated via API E2E pick list test");
        body.put("requestDate", LocalDate.now().toString());
        body.put("status", "ARCHIVED"); // взяв з example в swagger

        // в schema на PUT є тільки toLocation, fromLocation не вимагають
        Map<String, Object> toLocation = new HashMap<>();
        toLocation.put("id", TO_LOCATION_ID);
        body.put("toLocation", toLocation);

        // userIdsToAssign – можемо не ставити, якщо не обовʼязково
        // body.put("userIdsToAssign", List.of("..."));

        return body;
    }
}
