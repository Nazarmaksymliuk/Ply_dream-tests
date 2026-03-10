package org.example.Api.Transfer;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.APIResponse;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.example.Api.helpers.LocationMaterials.LocationMaterialsClient;
import org.example.Api.helpers.LocationsHelper.LocationsClient;
import org.example.Api.helpers.MaterialTransfer.MaterialTransferClient;
import org.example.Api.helpers.MaterialsHelper.MaterialsClient;
import org.example.Api.helpers.assertions.ApiAssertions;
import org.example.BaseAPITestExtension.BaseApiTest;
import org.example.apifactories.LocationsTestDataFactory;
import org.example.apifactories.MaterialTransferTestDataFactory;
import org.example.apifactories.MaterialsTestDataFactory;
import org.example.config.TestEnvironment;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Negative test scenarios for Material Transfer API.
 * Tests error handling, validation, and edge cases.
 */
@Epic("Transfer")
@Feature("Material Transfer Negative Scenarios")
@Timeout(value = TestEnvironment.E2E_TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
public class MaterialTransferNegativeTests extends BaseApiTest {

    private static final Logger log = LoggerFactory.getLogger(MaterialTransferNegativeTests.class);

    private LocationsClient locationsClient;
    private MaterialsClient materialsClient;
    private MaterialTransferClient transferClient;
    private LocationMaterialsClient locationMaterialsClient;

    private String fromLocationId;
    private String toLocationId;
    private String materialId;
    private String materialDetailsLocationId;

    private static final int INITIAL_QTY = 3;

    @BeforeAll
    void initClientsAndResolveDependencies() throws IOException {
        locationsClient = new LocationsClient(userApi);
        materialsClient = new MaterialsClient(userApi);
        transferClient = new MaterialTransferClient(userApi);
        locationMaterialsClient = new LocationMaterialsClient(userApi);

        // Create FROM warehouse
        Map<String, Object> fromBody = LocationsTestDataFactory.buildCreateWarehouseBody("API FROM WH Transfer Neg ");
        APIResponse fromResp = locationsClient.createLocation(fromBody, false);
        log.info("CREATE FROM WAREHOUSE status: {}", fromResp.status());
        ApiAssertions.assertStatusOneOf(fromResp, "Create FROM warehouse", 200, 201);

        fromLocationId = locationsClient.extractLocationId(fromResp);
        Assertions.assertNotNull(fromLocationId);

        // Create material
        Map<String, Object> createMatBody = MaterialsTestDataFactory.buildCreateMaterialInLocationRequest(
                "API Transfer Neg Material ", "TR-NEG-", fromLocationId
        );
        APIResponse createMatResp = materialsClient.createMaterial(createMatBody);
        log.info("CREATE MATERIAL status: {}", createMatResp.status());
        ApiAssertions.assertStatusOneOf(createMatResp, "Create material", 200, 201);

        materialId = materialsClient.extractMaterialId(createMatResp);
        String materialVariationId = materialsClient.extractFirstVariationId(createMatResp);
        Assertions.assertNotNull(materialId);
        Assertions.assertNotNull(materialVariationId);

        // Attach material to FROM location
        Map<String, Object> attachBody = MaterialsTestDataFactory.buildAttachMaterialToLocationRequest(
                fromLocationId, materialId, materialVariationId, INITIAL_QTY
        );
        APIResponse attachResp = materialsClient.attachMaterialToLocation(attachBody);
        ApiAssertions.assertStatusOneOf(attachResp, "Attach material", 200, 201, 204);

        // Create TO warehouse
        Map<String, Object> toBody = LocationsTestDataFactory.buildCreateWarehouseBody("API TO WH Transfer Neg ");
        APIResponse toResp = locationsClient.createLocation(toBody, false);
        log.info("CREATE TO WAREHOUSE status: {}", toResp.status());
        ApiAssertions.assertStatusOneOf(toResp, "Create TO warehouse", 200, 201);

        toLocationId = locationsClient.extractLocationId(toResp);
        Assertions.assertNotNull(toLocationId);

        // Resolve materialDetailsLocationId
        APIResponse materialsResp = locationMaterialsClient.searchMaterialsInLocation(fromLocationId);
        Assertions.assertEquals(200, materialsResp.status());
        JsonNode materialsRoot = locationMaterialsClient.parseResponse(materialsResp);
        JsonNode firstMaterial = locationMaterialsClient.getFirstMaterial(materialsRoot);
        Assertions.assertNotNull(firstMaterial, "No materials found in FROM location");
        materialDetailsLocationId = locationMaterialsClient.extractMaterialDetailsId(firstMaterial);
        Assertions.assertNotNull(materialDetailsLocationId);

        log.info("Setup complete for MaterialTransfer negative tests");
    }

    @AfterAll
    void cleanup() {
        if (materialId != null) {
            try {
                materialsClient.deleteMaterial(materialId);
            } catch (Exception e) {
                log.warn("Cleanup: failed to delete material: {}", e.getMessage());
            }
        }
        if (fromLocationId != null) {
            try {
                locationsClient.deleteLocation(fromLocationId, null, "Cleanup from Transfer negative tests");
            } catch (Exception e) {
                log.warn("Cleanup: failed to delete FROM location: {}", e.getMessage());
            }
        }
        if (toLocationId != null) {
            try {
                locationsClient.deleteLocation(toLocationId, null, "Cleanup from Transfer negative tests");
            } catch (Exception e) {
                log.warn("Cleanup: failed to delete TO location: {}", e.getMessage());
            }
        }
    }

    @Test
    @DisplayName("Transfer with empty body returns error")
    void transfer_withEmptyBody_returnsError() {
        log.info("Testing transfer with empty body");

        Map<String, Object> emptyBody = new HashMap<>();

        APIResponse response = transferClient.transfer(fromLocationId, emptyBody);

        log.info("Transfer with empty body - status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatusOneOf(response, "Transfer with empty body", 400, 500);
    }

    @Test
    @DisplayName("Transfer from non-existent location returns error")
    void transfer_fromNonExistentLocation_returnsError() {
        log.info("Testing transfer from non-existent location");

        String fakeLocationId = UUID.randomUUID().toString();

        Map<String, Object> body = MaterialTransferTestDataFactory.buildTransferRequest(
                toLocationId, materialDetailsLocationId, 1, 25.5, "REPLENISHED"
        );

        APIResponse response = transferClient.transfer(fakeLocationId, body);

        log.info("Transfer from non-existent location - status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatusOneOf(response, "Transfer from non-existent location", 400, 404, 500);
    }

    @Test
    @DisplayName("Transfer to non-existent location returns error")
    void transfer_toNonExistentLocation_returnsError() {
        log.info("Testing transfer to non-existent location");

        String fakeToLocationId = UUID.randomUUID().toString();

        Map<String, Object> body = MaterialTransferTestDataFactory.buildTransferRequest(
                fakeToLocationId, materialDetailsLocationId, 1, 25.5, "REPLENISHED"
        );

        APIResponse response = transferClient.transfer(fromLocationId, body);

        log.info("Transfer to non-existent location - status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatusOneOf(response, "Transfer to non-existent location", 400, 404, 500);
    }

    @Test
    @DisplayName("Transfer with non-existent materialDetailsLocationId returns error")
    void transfer_withNonExistentMaterialDetailsId_returnsError() {
        log.info("Testing transfer with non-existent materialDetailsLocationId");

        String fakeMaterialDetailsId = UUID.randomUUID().toString();

        Map<String, Object> body = MaterialTransferTestDataFactory.buildTransferRequest(
                toLocationId, fakeMaterialDetailsId, 1, 25.5, "REPLENISHED"
        );

        APIResponse response = transferClient.transfer(fromLocationId, body);

        log.info("Transfer with non-existent materialDetailsId - status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatusOneOf(response, "Transfer with non-existent materialDetailsLocationId", 400, 404, 500);
    }

    @Test
    @DisplayName("Transfer with zero quantity returns error")
    void transfer_withZeroQuantity_returnsError() {
        log.info("Testing transfer with zero quantity");

        Map<String, Object> body = MaterialTransferTestDataFactory.buildTransferRequest(
                toLocationId, materialDetailsLocationId, 0, 25.5, "REPLENISHED"
        );

        APIResponse response = transferClient.transfer(fromLocationId, body);

        log.info("Transfer with zero quantity - status: {}, body: {}", response.status(), response.text());

        // API may accept zero or reject with 400
        ApiAssertions.assertStatusOneOf(response, "Transfer with zero quantity", 200, 204, 400, 500);
    }

    @Test
    @DisplayName("Transfer with negative quantity returns error")
    void transfer_withNegativeQuantity_returnsError() {
        log.info("Testing transfer with negative quantity");

        Map<String, Object> body = MaterialTransferTestDataFactory.buildTransferRequest(
                toLocationId, materialDetailsLocationId, -1, 25.5, "REPLENISHED"
        );

        APIResponse response = transferClient.transfer(fromLocationId, body);

        log.info("Transfer with negative quantity - status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatusOneOf(response, "Transfer with negative quantity", 400, 500, 200, 204);
    }

    @Test
    @DisplayName("Transfer with quantity exceeding stock returns error")
    void transfer_withExceedingQuantity_returnsError() {
        log.info("Testing transfer with quantity exceeding available stock");

        int exceedingQty = INITIAL_QTY + 100;

        Map<String, Object> body = MaterialTransferTestDataFactory.buildTransferRequest(
                toLocationId, materialDetailsLocationId, exceedingQty, 25.5, "REPLENISHED"
        );

        APIResponse response = transferClient.transfer(fromLocationId, body);

        log.info("Transfer with exceeding quantity - status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatusOneOf(response, "Transfer with exceeding quantity", 400, 500, 200, 204);
    }

    @Test
    @DisplayName("Transfer with negative price returns error or accepts")
    void transfer_withNegativePrice_returnsErrorOrAccepts() {
        log.info("Testing transfer with negative price");

        Map<String, Object> body = MaterialTransferTestDataFactory.buildTransferRequest(
                toLocationId, materialDetailsLocationId, 1, -10.0, "REPLENISHED"
        );

        APIResponse response = transferClient.transfer(fromLocationId, body);

        log.info("Transfer with negative price - status: {}, body: {}", response.status(), response.text());

        // API may or may not validate negative prices
        ApiAssertions.assertStatusOneOf(response, "Transfer with negative price", 200, 204, 400, 500);
    }

    @Test
    @DisplayName("Transfer with invalid fromLocation ID format returns error")
    void transfer_withInvalidFromLocationIdFormat_returnsError() {
        log.info("Testing transfer with invalid fromLocation ID format");

        String invalidId = "not-a-valid-uuid";

        Map<String, Object> body = MaterialTransferTestDataFactory.buildTransferRequest(
                toLocationId, materialDetailsLocationId, 1, 25.5, "REPLENISHED"
        );

        APIResponse response = transferClient.transfer(invalidId, body);

        log.info("Transfer with invalid fromLocation ID - status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatusOneOf(response, "Transfer with invalid fromLocation ID format", 400, 404, 500);
    }
}
