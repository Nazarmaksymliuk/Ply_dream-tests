package org.example.Api.PickLists;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.APIResponse;
import org.example.Api.helpers.ConsumablesHelper.ConsumablesClient;
import org.example.Api.helpers.LocationConsumables.LocationConsumablesClient;
import org.example.Api.helpers.LocationMaterials.LocationMaterialsClient;
import org.example.Api.helpers.MaterialsHelper.MaterialsClient;
import org.example.Api.helpers.MeasurementUnits.MeasurementUnitsClient;
import org.example.Api.helpers.PickListsHelper.PickListsClient;
import org.example.BaseAPITestExtension.BaseApiTest;
import org.example.apifactories.ConsumablesTestDataFactory;
import org.example.apifactories.MaterialsTestDataFactory;
import org.example.apifactories.PickListsTestDataFactory;
import org.example.config.TestEnvironment;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Timeout;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Timeout(value = TestEnvironment.E2E_TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
public class PickListsE2ETests extends BaseApiTest {

    private static final Logger log = LoggerFactory.getLogger(PickListsE2ETests.class);

    private PickListsClient pickListsClient;
    private LocationMaterialsClient locationMaterialsClient;
    private LocationConsumablesClient locationConsumablesClient;
    private MaterialsClient materialsClient;
    private ConsumablesClient consumablesClient;
    private MeasurementUnitsClient measurementUnitsClient;

    private static final String FROM_LOCATION_ID = TestEnvironment.WAREHOUSE_MAIN_ID;
    private static final String TO_LOCATION_ID   = TestEnvironment.WAREHOUSE_TRANSFER_ID;

    private String materialDetailsFromLocationId;
    private String consumableUnitFromLocationId;

    private String pickListId;

    private String createdMaterialId;
    private String createdConsumableId;

    private Map<String, Object> lastCreateBody;
    private Map<String, Object> lastUpdateBody;

    @BeforeAll
    void initClientsAndResolveDependencies() throws IOException {
        pickListsClient = new PickListsClient(userApi);
        locationMaterialsClient = new LocationMaterialsClient(userApi);
        locationConsumablesClient = new LocationConsumablesClient(userApi);
        materialsClient = new MaterialsClient(userApi);
        consumablesClient = new ConsumablesClient(userApi);
        measurementUnitsClient = new MeasurementUnitsClient(userApi);

        // 1) Create material in FROM location (to guarantee it exists)
        Map<String, Object> materialBody = MaterialsTestDataFactory.buildCreateMaterialInLocationRequest(
                "API Material For PickLists ",
                "MAT-PL-",
                FROM_LOCATION_ID
        );

        APIResponse createMatResp = materialsClient.createMaterial(materialBody);
        log.info("CREATE MATERIAL status: {}", createMatResp.status());
        log.debug("CREATE MATERIAL body: {}", createMatResp.text());
        Assertions.assertEquals(201, createMatResp.status(), "Expected 201 on material create");

        createdMaterialId = materialsClient.extractMaterialId(createMatResp);
        Assertions.assertNotNull(createdMaterialId, "createdMaterialId must not be null");
        log.info("Created material for PickLists: {}", createdMaterialId);

        // Search materials in location to get materialDetailsFromLocationId
        APIResponse materialsResp = locationMaterialsClient.searchMaterialsInLocation(FROM_LOCATION_ID);
        log.info("MATERIALS SEARCH status: {}", materialsResp.status());
        Assertions.assertEquals(200, materialsResp.status(), "Expected 200 from /v2/locations/{id}/materials/search");

        JsonNode materialsRoot = locationMaterialsClient.parseResponse(materialsResp);
        JsonNode firstMaterial = locationMaterialsClient.getFirstMaterial(materialsRoot);
        Assertions.assertNotNull(firstMaterial, "No materials found in fromLocation after creating one");

        materialDetailsFromLocationId = locationMaterialsClient.extractMaterialDetailsId(firstMaterial);
        Assertions.assertNotNull(materialDetailsFromLocationId, "materialDetailsFromLocationId could not be extracted");
        log.info("Resolved materialDetailsFromLocationId: {}", materialDetailsFromLocationId);

        // 2) Get measurement units (needed for consumable creation)
        APIResponse muResp = measurementUnitsClient.getMeasurementUnits();
        Assertions.assertEquals(200, muResp.status(), "Expected 200 from /materials/measurement-units");

        JsonNode muRoot = measurementUnitsClient.parseResponse(muResp);
        JsonNode eachUnit = measurementUnitsClient.findUnit(muRoot, "EA", "Each");
        Assertions.assertNotNull(eachUnit, "MeasurementUnit 'Each/EA' not found");

        String muId = eachUnit.get("id").asText();
        String muName = eachUnit.get("name").asText();
        String muAbbr = eachUnit.get("abbreviation").asText();

        // 3) Create consumable with unit in FROM location
        Map<String, Object> consumableBody = ConsumablesTestDataFactory.buildCreateConsumableWithLocationBody(
                "API Consumable For PickLists ",
                "CNS-PL-",
                "API Tag PL ",
                muId, muName, muAbbr,
                FROM_LOCATION_ID,
                10
        );

        APIResponse createConsResp = consumablesClient.createConsumable(consumableBody);
        log.info("CREATE CONSUMABLE status: {}", createConsResp.status());
        log.debug("CREATE CONSUMABLE body: {}", createConsResp.text());
        Assertions.assertEquals(201, createConsResp.status(), "Expected 201 on consumable create");

        createdConsumableId = consumablesClient.extractConsumableId(createConsResp);
        Assertions.assertNotNull(createdConsumableId, "createdConsumableId must not be null");
        log.info("Created consumable for PickLists: {}", createdConsumableId);

        // Search consumables in location to get consumableUnitFromLocationId
        APIResponse consResp = locationConsumablesClient.getConsumablesInLocation(FROM_LOCATION_ID);
        log.info("CONSUMABLES IN LOCATION status: {}", consResp.status());
        Assertions.assertEquals(200, consResp.status(), "Expected 200 from /locations/{id}/consumables");

        JsonNode consRoot = locationConsumablesClient.parseResponse(consResp);
        JsonNode firstConsumable = locationConsumablesClient.getFirstConsumable(consRoot);
        Assertions.assertNotNull(firstConsumable, "No consumables found in fromLocation after creating one");

        consumableUnitFromLocationId = locationConsumablesClient.extractConsumableUnitId(firstConsumable);
        Assertions.assertNotNull(consumableUnitFromLocationId, "consumableUnitFromLocationId could not be extracted");
        log.info("Resolved consumableUnitFromLocationId: {}", consumableUnitFromLocationId);
    }

    @AfterAll
    void cleanup() {
        if (createdConsumableId != null) {
            try {
                APIResponse r = consumablesClient.deleteConsumables(Collections.singletonList(createdConsumableId));
                log.info("CLEANUP DELETE CONSUMABLE status: {}", r.status());
            } catch (Exception e) {
                log.warn("CLEANUP DELETE CONSUMABLE failed: {}", e.getMessage());
            }
        }

        if (createdMaterialId != null) {
            try {
                APIResponse r = materialsClient.deleteMaterial(createdMaterialId);
                log.info("CLEANUP DELETE MATERIAL status: {}", r.status());
            } catch (Exception e) {
                log.warn("CLEANUP DELETE MATERIAL failed: {}", e.getMessage());
            }
        }
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

        log.info("CREATE PICK LIST status: {}", status);
        log.debug("CREATE PICK LIST body: {}", response.text());

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

        log.info("UPDATE PICK LIST status: {}", status);
        log.debug("UPDATE PICK LIST body: {}", response.text());

        Assertions.assertTrue(status == 200 || status == 201, "Expected 200 or 201 on update pick list, but got: " + status);

        JsonNode updated = pickListsClient.parsePickList(response);

        Assertions.assertEquals(pickListId, updated.get("id").asText(), "Updated pickList id must be the same as path id");
        Assertions.assertEquals(body.get("name"), updated.get("name").asText(), "Updated name must match request");
        Assertions.assertEquals(body.get("note"), updated.get("note").asText(), "Updated note must match request");

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

        log.info("DELETE PICK LIST status: {}", status);
        log.debug("DELETE PICK LIST body: '{}'", response.text());

        Assertions.assertTrue(status == 204 || status == 200, "Expected 204 or 200 on delete pick list, but got: " + status);
    }
}
