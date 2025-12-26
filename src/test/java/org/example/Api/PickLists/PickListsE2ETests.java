package org.example.Api.PickLists;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.APIResponse;
import org.example.Api.helpers.LocationConsumables.LocationConsumablesClient;
import org.example.Api.helpers.LocationMaterials.LocationMaterialsClient;
import org.example.Api.helpers.PickListsHelper.PickListsClient;
import org.example.BaseAPITestExtension.BaseApiTest;
import org.example.apifactories.PickListsTestDataFactory;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.Map;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PickListsE2ETests extends BaseApiTest {

    private PickListsClient pickListsClient;
    private LocationMaterialsClient locationMaterialsClient;
    private LocationConsumablesClient locationConsumablesClient;

    private static final String FROM_LOCATION_ID = "ac1f56fd-9919-137e-8199-1f504b6607e8"; // WarehouseMain
    private static final String TO_LOCATION_ID   = "ac1f56fd-9a4a-154f-819a-4c1fc3ea0711"; // WarehouseToTransfer

    private String materialDetailsFromLocationId;
    private String consumableUnitFromLocationId;

    private String pickListId;

    // якщо хочеш тримати для дебага/реюзу
    private Map<String, Object> lastCreateBody;
    private Map<String, Object> lastUpdateBody;

    @BeforeAll
    void initClientsAndResolveDependencies() throws IOException {
        pickListsClient = new PickListsClient(userApi);
        locationMaterialsClient = new LocationMaterialsClient(userApi);
        locationConsumablesClient = new LocationConsumablesClient(userApi);

        // 1) MATERIAL DETAILS з WarehouseMain
        APIResponse materialsResp = locationMaterialsClient.searchMaterialsInLocation(FROM_LOCATION_ID);
        System.out.println("MATERIALS SEARCH status: " + materialsResp.status());
        System.out.println("MATERIALS SEARCH body: " + materialsResp.text());

        Assertions.assertEquals(200, materialsResp.status(), "Expected 200 from /v2/locations/{id}/materials/search");

        JsonNode materialsRoot = locationMaterialsClient.parseResponse(materialsResp);
        JsonNode firstMaterial = locationMaterialsClient.getFirstMaterial(materialsRoot);

        Assertions.assertNotNull(firstMaterial, "No materials found in fromLocation – please ensure warehouse has at least one material");

        materialDetailsFromLocationId = locationMaterialsClient.extractMaterialDetailsId(firstMaterial);
        Assertions.assertNotNull(materialDetailsFromLocationId, "materialDetailsFromLocationId could not be extracted");

        System.out.println("Resolved materialDetailsFromLocationId: " + materialDetailsFromLocationId);

        // 2) CONSUMABLE з WarehouseMain
        APIResponse consResp = locationConsumablesClient.getConsumablesInLocation(FROM_LOCATION_ID);
        System.out.println("CONSUMABLES IN LOCATION status: " + consResp.status());
        System.out.println("CONSUMABLES IN LOCATION body: " + consResp.text());

        Assertions.assertEquals(200, consResp.status(), "Expected 200 from /locations/{id}/consumables");

        JsonNode consRoot = locationConsumablesClient.parseResponse(consResp);
        JsonNode firstConsumable = locationConsumablesClient.getFirstConsumable(consRoot);

        Assertions.assertNotNull(firstConsumable, "No consumables found in fromLocation – please ensure at least one consumable in warehouse");

        consumableUnitFromLocationId = locationConsumablesClient.extractConsumableUnitId(firstConsumable);
        Assertions.assertNotNull(consumableUnitFromLocationId, "consumableUnitFromLocationId could not be extracted");

        System.out.println("Resolved consumableUnitFromLocationId: " + consumableUnitFromLocationId);
    }

    @Test
    @Order(1)
    void createPickList_withMaterialAndConsumable_createsOk() throws IOException {
        Map<String, Object> body = PickListsTestDataFactory.buildCreatePickListBody(
                "API Pick List ",
                FROM_LOCATION_ID,
                TO_LOCATION_ID,
                materialDetailsFromLocationId,
                consumableUnitFromLocationId
        );
        lastCreateBody = body;

        APIResponse response = pickListsClient.createPickList(body);
        int status = response.status();

        System.out.println("CREATE PICK LIST status: " + status);
        System.out.println("CREATE PICK LIST body: " + response.text());

        Assertions.assertTrue(status == 201 || status == 200, "Expected 201 or 200 on create pick list, but got: " + status);

        pickListId = pickListsClient.extractPickListId(response);
        Assertions.assertNotNull(pickListId, "pickListId must not be null after create");
        Assertions.assertFalse(pickListId.isEmpty(), "pickListId must not be empty");

        JsonNode created = pickListsClient.parsePickList(response);

        Assertions.assertEquals(body.get("name"), created.get("name").asText(), "Pick list name must match request");

        JsonNode fromLoc = created.get("fromLocation");
        JsonNode toLoc = created.get("toLocation");
        Assertions.assertNotNull(fromLoc, "fromLocation must be present in response");
        Assertions.assertNotNull(toLoc, "toLocation must be present in response");

        Assertions.assertEquals(FROM_LOCATION_ID, fromLoc.get("id").asText(), "fromLocation.id must match FROM_LOCATION_ID");
        Assertions.assertEquals(TO_LOCATION_ID, toLoc.get("id").asText(), "toLocation.id must match TO_LOCATION_ID");

        Assertions.assertNotNull(created.get("status"), "status in pick list response must not be null");
    }

    @Test
    @Order(2)
    void updatePickList_updatesNameStatusAndToLocation() throws IOException {
        Assertions.assertNotNull(pickListId, "pickListId is null – create test probably failed");

        Map<String, Object> body = PickListsTestDataFactory.buildUpdatePickListBody(
                pickListId,
                "API Pick List UPDATED ",
                TO_LOCATION_ID
        );
        lastUpdateBody = body;

        APIResponse response = pickListsClient.updatePickList(pickListId, body);
        int status = response.status();

        System.out.println("UPDATE PICK LIST status: " + status);
        System.out.println("UPDATE PICK LIST body: " + response.text());

        Assertions.assertTrue(status == 200 || status == 201, "Expected 200 or 201 on update pick list, but got: " + status);

        JsonNode updated = pickListsClient.parsePickList(response);

        Assertions.assertEquals(pickListId, updated.get("id").asText(), "Updated pickList id must be the same as path id");
        Assertions.assertEquals(body.get("name"), updated.get("name").asText(), "Updated name must match request");
        Assertions.assertEquals(body.get("note"), updated.get("note").asText(), "Updated note must match request");

        // status може бути null, але поле має бути присутнє
        Assertions.assertNotNull(updated.get("status"), "status field must be present in response (can be null by backend design)");

        JsonNode toLoc = updated.get("toLocation");
        Assertions.assertNotNull(toLoc, "toLocation must be present in update response");
        Assertions.assertEquals(TO_LOCATION_ID, toLoc.get("id").asText(), "Updated toLocation.id must still match TO_LOCATION_ID");
    }

    @Test
    @Order(3)
    void deletePickList_deletesSuccessfully() {
        Assertions.assertNotNull(pickListId, "pickListId is null – previous tests probably failed");

        APIResponse response = pickListsClient.deletePickListById(pickListId);
        int status = response.status();

        System.out.println("DELETE PICK LIST status: " + status);
        System.out.println("DELETE PICK LIST body: '" + response.text() + "'");

        Assertions.assertTrue(status == 204 || status == 200, "Expected 204 or 200 on delete pick list, but got: " + status);
    }
}
