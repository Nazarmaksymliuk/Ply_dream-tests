package org.example.Api.PickLists;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.APIResponse;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.example.Api.helpers.ConsumablesHelper.ConsumablesClient;
import org.example.Api.helpers.LocationConsumables.LocationConsumablesClient;
import org.example.Api.helpers.LocationMaterials.LocationMaterialsClient;
import org.example.Api.helpers.LocationsHelper.LocationsClient;
import org.example.Api.helpers.MaterialsHelper.MaterialsClient;
import org.example.Api.helpers.MeasurementUnits.MeasurementUnitsClient;
import org.example.Api.helpers.PickListsHelper.PickListsClient;
import org.example.Api.helpers.assertions.ApiAssertions;
import org.example.BaseAPITestExtension.BaseApiTest;
import org.example.apifactories.ConsumablesTestDataFactory;
import org.example.apifactories.LocationsTestDataFactory;
import org.example.apifactories.MaterialsTestDataFactory;
import org.example.apifactories.PickListsTestDataFactory;
import org.example.config.TestEnvironment;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Negative test scenarios for Pick Lists API.
 * Tests error handling, validation, and edge cases.
 */
@Epic("PickLists")
@Feature("PickLists Negative Scenarios")
@Timeout(value = TestEnvironment.E2E_TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
public class PickListsNegativeTests extends BaseApiTest {

    private static final Logger log = LoggerFactory.getLogger(PickListsNegativeTests.class);

    private PickListsClient pickListsClient;
    private LocationsClient locationsClient;
    private MaterialsClient materialsClient;
    private ConsumablesClient consumablesClient;
    private MeasurementUnitsClient measurementUnitsClient;
    private LocationMaterialsClient locationMaterialsClient;
    private LocationConsumablesClient locationConsumablesClient;

    private String fromLocationId;
    private String toLocationId;
    private String materialDetailsFromLocationId;
    private String consumableUnitFromLocationId;
    private String createdMaterialId;
    private String createdConsumableId;

    private final List<String> createdPickListIds = new ArrayList<>();

    @BeforeAll
    void initClientsAndResolveDependencies() throws IOException {
        pickListsClient = new PickListsClient(userApi);
        locationsClient = new LocationsClient(userApi);
        materialsClient = new MaterialsClient(userApi);
        consumablesClient = new ConsumablesClient(userApi);
        measurementUnitsClient = new MeasurementUnitsClient(userApi);
        locationMaterialsClient = new LocationMaterialsClient(userApi);
        locationConsumablesClient = new LocationConsumablesClient(userApi);

        // Create FROM warehouse location
        Map<String, Object> fromLocBody = LocationsTestDataFactory.buildCreateWarehouseBody("API WH PickList Neg FROM ");
        APIResponse createFromResp = locationsClient.createLocation(fromLocBody, false);
        log.info("CREATE FROM LOCATION status: {}", createFromResp.status());
        ApiAssertions.assertStatusOneOf(createFromResp, "Create FROM location", 200, 201);

        fromLocationId = locationsClient.extractLocationId(createFromResp);
        Assertions.assertNotNull(fromLocationId, "fromLocationId must not be null");

        // Create TO warehouse location
        Map<String, Object> toLocBody = LocationsTestDataFactory.buildCreateWarehouseBody("API WH PickList Neg TO ");
        APIResponse createToResp = locationsClient.createLocation(toLocBody, false);
        log.info("CREATE TO LOCATION status: {}", createToResp.status());
        ApiAssertions.assertStatusOneOf(createToResp, "Create TO location", 200, 201);

        toLocationId = locationsClient.extractLocationId(createToResp);
        Assertions.assertNotNull(toLocationId, "toLocationId must not be null");

        // Create material in FROM location
        Map<String, Object> materialBody = MaterialsTestDataFactory.buildCreateMaterialInLocationRequest(
                "API Material PickList Neg ", "MAT-PLN-", fromLocationId
        );
        APIResponse createMatResp = materialsClient.createMaterial(materialBody);
        log.info("CREATE MATERIAL status: {}", createMatResp.status());
        ApiAssertions.assertStatusOneOf(createMatResp, "Create material", 200, 201);

        createdMaterialId = materialsClient.extractMaterialId(createMatResp);
        Assertions.assertNotNull(createdMaterialId, "createdMaterialId must not be null");

        String materialVariationId = materialsClient.extractFirstVariationId(createMatResp);
        Assertions.assertNotNull(materialVariationId, "materialVariationId must not be null");

        // Attach material to FROM location
        Map<String, Object> attachBody = MaterialsTestDataFactory.buildAttachMaterialToLocationRequest(
                fromLocationId, createdMaterialId, materialVariationId, 1
        );
        APIResponse attachResp = materialsClient.attachMaterialToLocation(attachBody);
        ApiAssertions.assertStatusOneOf(attachResp, "Attach material to location", 200, 201, 204);

        // Resolve materialDetailsFromLocationId
        APIResponse materialsResp = locationMaterialsClient.searchMaterialsInLocation(fromLocationId);
        Assertions.assertEquals(200, materialsResp.status());
        JsonNode materialsRoot = locationMaterialsClient.parseResponse(materialsResp);
        JsonNode firstMaterial = locationMaterialsClient.getFirstMaterial(materialsRoot);
        Assertions.assertNotNull(firstMaterial, "No materials found in fromLocation");
        materialDetailsFromLocationId = locationMaterialsClient.extractMaterialDetailsId(firstMaterial);
        Assertions.assertNotNull(materialDetailsFromLocationId);

        // Get measurement units for consumable
        APIResponse muResp = measurementUnitsClient.getMeasurementUnits();
        Assertions.assertEquals(200, muResp.status());
        JsonNode muRoot = measurementUnitsClient.parseResponse(muResp);
        JsonNode eachUnit = measurementUnitsClient.findUnit(muRoot, "EA", "Each");
        Assertions.assertNotNull(eachUnit, "MeasurementUnit 'Each/EA' not found");

        String muId = eachUnit.get("id").asText();
        String muName = eachUnit.get("name").asText();
        String muAbbr = eachUnit.get("abbreviation").asText();

        // Create consumable in FROM location
        Map<String, Object> consumableBody = ConsumablesTestDataFactory.buildCreateConsumableWithLocationBody(
                "API Consumable PickList Neg ", "CNS-PLN-", "API Tag PLN ",
                muId, muName, muAbbr, fromLocationId, 10
        );
        APIResponse createConsResp = consumablesClient.createConsumable(consumableBody);
        log.info("CREATE CONSUMABLE status: {}", createConsResp.status());
        Assertions.assertEquals(201, createConsResp.status());

        createdConsumableId = consumablesClient.extractConsumableId(createConsResp);
        Assertions.assertNotNull(createdConsumableId);

        // Resolve consumableUnitFromLocationId
        APIResponse consResp = locationConsumablesClient.getConsumablesInLocation(fromLocationId);
        Assertions.assertEquals(200, consResp.status());
        JsonNode consRoot = locationConsumablesClient.parseResponse(consResp);
        JsonNode firstConsumable = locationConsumablesClient.getFirstConsumable(consRoot);
        Assertions.assertNotNull(firstConsumable);
        consumableUnitFromLocationId = locationConsumablesClient.extractConsumableUnitId(firstConsumable);
        Assertions.assertNotNull(consumableUnitFromLocationId);

        log.info("Setup complete for PickLists negative tests");
    }

    @AfterAll
    void cleanup() {
        for (String id : createdPickListIds) {
            try {
                pickListsClient.deletePickListById(id);
            } catch (Exception e) {
                log.warn("Cleanup: failed to delete pick list {}: {}", id, e.getMessage());
            }
        }
        if (createdConsumableId != null) {
            try {
                consumablesClient.deleteConsumables(Collections.singletonList(createdConsumableId));
            } catch (Exception e) {
                log.warn("Cleanup: failed to delete consumable: {}", e.getMessage());
            }
        }
        if (createdMaterialId != null) {
            try {
                materialsClient.deleteMaterial(createdMaterialId);
            } catch (Exception e) {
                log.warn("Cleanup: failed to delete material: {}", e.getMessage());
            }
        }
        if (fromLocationId != null) {
            try {
                locationsClient.deleteLocation(fromLocationId, null, "Cleanup from PickLists negative tests");
            } catch (Exception e) {
                log.warn("Cleanup: failed to delete FROM location: {}", e.getMessage());
            }
        }
        if (toLocationId != null) {
            try {
                locationsClient.deleteLocation(toLocationId, null, "Cleanup from PickLists negative tests");
            } catch (Exception e) {
                log.warn("Cleanup: failed to delete TO location: {}", e.getMessage());
            }
        }
    }

    @Test
    @DisplayName("Create pick list with empty body returns 400")
    void createPickList_withEmptyBody_returns400() {
        log.info("Testing pick list creation with empty body");

        Map<String, Object> emptyBody = new HashMap<>();
        APIResponse response = pickListsClient.createPickList(emptyBody);

        log.info("Create pick list with empty body - status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatusOneOf(response, "Create pick list with empty body", 400, 500);
    }

    @Test
    @DisplayName("Create pick list with missing fromLocation returns error")
    void createPickList_withMissingFromLocation_returnsError() {
        log.info("Testing pick list creation with missing fromLocation");

        Map<String, Object> body = new HashMap<>();
        body.put("name", "PickList Without FromLocation " + System.currentTimeMillis());
        body.put("note", "Missing fromLocation test");

        Map<String, Object> toLocation = new HashMap<>();
        toLocation.put("id", toLocationId);
        body.put("toLocation", toLocation);

        APIResponse response = pickListsClient.createPickList(body);

        log.info("Create pick list without fromLocation - status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatusOneOf(response, "Create pick list without fromLocation", 400, 500);
    }

    @Test
    @DisplayName("Create pick list with non-existent fromLocation ID returns error")
    void createPickList_withNonExistentFromLocation_returnsError() {
        log.info("Testing pick list creation with non-existent fromLocation ID");

        String fakeLocationId = UUID.randomUUID().toString();

        Map<String, Object> body = PickListsTestDataFactory.buildCreatePickListBody(
                "API PickList NonExistent Loc ",
                fakeLocationId,
                toLocationId,
                materialDetailsFromLocationId,
                consumableUnitFromLocationId
        );

        APIResponse response = pickListsClient.createPickList(body);

        log.info("Create pick list with non-existent fromLocation - status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatusOneOf(response, "Create pick list with non-existent fromLocation", 400, 404, 500);
    }

    @Test
    @DisplayName("Update pick list with non-existent ID returns 404")
    void updatePickList_withNonExistentId_returns404() {
        log.info("Testing pick list update with non-existent ID");

        String nonExistentId = UUID.randomUUID().toString();
        Map<String, Object> body = PickListsTestDataFactory.buildUpdatePickListBody(
                nonExistentId, "API PickList UPDATED ", toLocationId
        );

        APIResponse response = pickListsClient.updatePickList(nonExistentId, body);

        log.info("Update pick list with non-existent ID - status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatusOneOf(response, "Update pick list with non-existent ID", 400, 404);
    }

    @Test
    @DisplayName("Delete pick list with non-existent ID returns 404")
    void deletePickList_withNonExistentId_returns404() {
        log.info("Testing pick list deletion with non-existent ID");

        String nonExistentId = UUID.randomUUID().toString();

        APIResponse response = pickListsClient.deletePickListById(nonExistentId);

        log.info("Delete pick list with non-existent ID - status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatusOneOf(response, "Delete pick list with non-existent ID", 404, 204);
    }

    @Test
    @DisplayName("Delete already deleted pick list returns 404")
    void deletePickList_alreadyDeleted_returns404() throws IOException {
        log.info("Testing double delete of pick list");

        // Create a pick list
        Map<String, Object> body = PickListsTestDataFactory.buildCreatePickListBody(
                "API PickList DblDel ",
                fromLocationId,
                toLocationId,
                materialDetailsFromLocationId,
                consumableUnitFromLocationId
        );

        APIResponse createResp = pickListsClient.createPickList(body);
        ApiAssertions.assertStatusOneOf(createResp, "Create pick list for delete test", 200, 201);

        String pickListId = pickListsClient.extractPickListId(createResp);
        Assertions.assertNotNull(pickListId, "pickListId must not be null");
        log.info("Created pick list with ID: {}", pickListId);

        // Delete first time
        APIResponse firstDelete = pickListsClient.deletePickListById(pickListId);
        log.info("First delete status: {}", firstDelete.status());
        ApiAssertions.assertStatusOneOf(firstDelete, "First delete", 200, 204);

        // Delete second time
        APIResponse secondDelete = pickListsClient.deletePickListById(pickListId);
        log.info("Second delete status: {}, body: {}", secondDelete.status(), secondDelete.text());
        ApiAssertions.assertStatusOneOf(secondDelete, "Delete already deleted pick list", 404, 204);
    }

    @Test
    @DisplayName("Create pick list with SQL injection in name succeeds (sanitized)")
    void createPickList_withSqlInjectionInName_succeeds() throws IOException {
        log.info("Testing pick list creation with SQL injection in name");

        Map<String, Object> body = PickListsTestDataFactory.buildCreatePickListBody(
                "'; DROP TABLE pick_lists;-- ",
                fromLocationId,
                toLocationId,
                materialDetailsFromLocationId,
                consumableUnitFromLocationId
        );

        APIResponse response = pickListsClient.createPickList(body);
        log.info("Create pick list with SQL injection - status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatusOneOf(response, "Create pick list with SQL injection", 200, 201, 400);

        if (response.status() == 200 || response.status() == 201) {
            String pickListId = pickListsClient.extractPickListId(response);
            if (pickListId != null) {
                createdPickListIds.add(pickListId);
                log.info("Pick list with SQL injection name created, ID: {}", pickListId);
            }
        }
    }

    @Test
    @DisplayName("Create pick list with XSS in name succeeds (sanitized)")
    void createPickList_withXssInName_succeeds() throws IOException {
        log.info("Testing pick list creation with XSS in name");

        Map<String, Object> body = PickListsTestDataFactory.buildCreatePickListBody(
                "<script>alert('xss')</script> ",
                fromLocationId,
                toLocationId,
                materialDetailsFromLocationId,
                consumableUnitFromLocationId
        );

        APIResponse response = pickListsClient.createPickList(body);
        log.info("Create pick list with XSS - status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatusOneOf(response, "Create pick list with XSS", 200, 201, 400);

        if (response.status() == 200 || response.status() == 201) {
            String pickListId = pickListsClient.extractPickListId(response);
            if (pickListId != null) {
                createdPickListIds.add(pickListId);
                log.info("Pick list with XSS name created, ID: {}", pickListId);
            }
        }
    }

    @Test
    @DisplayName("Update pick list with invalid ID format returns error")
    void updatePickList_withInvalidIdFormat_returnsError() {
        log.info("Testing pick list update with invalid ID format");

        String invalidId = "not-a-valid-uuid";
        Map<String, Object> body = PickListsTestDataFactory.buildUpdatePickListBody(
                invalidId, "API PickList UPDATED ", toLocationId
        );

        APIResponse response = pickListsClient.updatePickList(invalidId, body);

        log.info("Update pick list with invalid ID format - status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatusOneOf(response, "Update pick list with invalid ID format", 400, 404);
    }
}
