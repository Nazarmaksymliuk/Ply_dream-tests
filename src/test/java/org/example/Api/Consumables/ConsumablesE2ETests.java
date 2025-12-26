package org.example.Api.Consumables;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.APIResponse;
import org.example.Api.helpers.ConsumablesHelper.ConsumablesClient;
import org.example.Api.helpers.MeasurementUnits.MeasurementUnitsClient;
import org.example.BaseAPITestExtension.BaseApiTest;
import org.example.apifactories.ConsumablesTestDataFactory;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ConsumablesE2ETests extends BaseApiTest {

    private ConsumablesClient consumablesClient;
    private MeasurementUnitsClient measurementUnitsClient;

    private String consumableId;

    // measurement units, які дістанемо з /materials/measurement-units
    private JsonNode eachMeasurementUnit;
    private JsonNode kgMeasurementUnit; // або fallback на Each

    @BeforeAll
    void initClient() throws IOException {
        consumablesClient = new ConsumablesClient(userApi);
        measurementUnitsClient = new MeasurementUnitsClient(userApi);

        APIResponse muResponse = measurementUnitsClient.getMeasurementUnits();
        int status = muResponse.status();

        System.out.println("MEASUREMENT UNITS status: " + status);
        System.out.println("MEASUREMENT UNITS body: " + muResponse.text());

        Assertions.assertEquals(200, status, "Expected 200 from /materials/measurement-units");

        JsonNode root = measurementUnitsClient.parseResponse(muResponse);

        eachMeasurementUnit = measurementUnitsClient.findUnit(root, "EA", "Each");
        Assertions.assertNotNull(eachMeasurementUnit, "MeasurementUnit 'Each/EA' not found in /materials/measurement-units");

        kgMeasurementUnit = measurementUnitsClient.findUnit(root, "KG", "Kilogram");
        if (kgMeasurementUnit == null) {
            System.out.println("KG measurement unit not found, using EACH for update as well");
            kgMeasurementUnit = eachMeasurementUnit;
        }
    }

    // 1️⃣ CREATE: POST /consumables/create
    @Test
    @Order(1)
    void createConsumable_createsAndStoresId() throws IOException {
        Map<String, Object> body = ConsumablesTestDataFactory.buildCreateConsumableBody(
                "API Consumable ",
                "CNS-",
                "API Tag ",
                eachMeasurementUnit.get("id").asText(),
                eachMeasurementUnit.get("name").asText(),
                eachMeasurementUnit.get("abbreviation").asText()
        );

        APIResponse response = consumablesClient.createConsumable(body);
        int status = response.status();

        System.out.println("CREATE CONSUMABLE status: " + status);
        System.out.println("CREATE CONSUMABLE body: " + response.text());

        Assertions.assertEquals(
                201,
                status,
                "Expected 201 on create consumable, but got: " + status
        );

        consumableId = consumablesClient.extractConsumableId(response);
        Assertions.assertNotNull(consumableId, "consumableId must not be null after create");
        Assertions.assertFalse(consumableId.isEmpty(), "consumableId must not be empty");

        JsonNode created = consumablesClient.parseConsumable(response);

        Assertions.assertEquals(body.get("name"), created.get("name").asText(), "Created name must match request");
        Assertions.assertEquals(body.get("itemNumber"), created.get("itemNumber").asText(), "Created itemNumber must match request");
        Assertions.assertEquals(
                ((Number) body.get("costForBusiness")).doubleValue(),
                created.get("costForBusiness").asDouble(),
                0.0001,
                "Created costForBusiness must match request"
        );

        Map<String, Object> muReq = (Map<String, Object>) body.get("measurementUnit");
        JsonNode muResp = created.get("measurementUnit");

        Assertions.assertEquals(muReq.get("id"), muResp.get("id").asText(), "measurementUnit.id must match request");
        Assertions.assertEquals(muReq.get("name"), muResp.get("name").asText(), "measurementUnit.name must match request");
        Assertions.assertEquals(muReq.get("abbreviation"), muResp.get("abbreviation").asText(), "measurementUnit.abbreviation must match request");
    }

    // 2️⃣ UPDATE: PUT /consumables/{id}
    @Test
    @Order(2)
    void updateConsumable_updatesAllMainFields() throws IOException {
        Assertions.assertNotNull(consumableId, "consumableId is null – create test probably failed");

        Map<String, Object> body = ConsumablesTestDataFactory.buildUpdateConsumableBody(
                consumableId,
                "API Consumable UPDATED ",
                "CNS-UPD-",
                "API Tag Updated ",
                kgMeasurementUnit.get("id").asText(),
                kgMeasurementUnit.get("name").asText(),
                kgMeasurementUnit.get("abbreviation").asText()
        );

        APIResponse response = consumablesClient.updateConsumable(consumableId, body);
        int status = response.status();

        System.out.println("UPDATE CONSUMABLE status: " + status);
        System.out.println("UPDATE CONSUMABLE body: " + response.text());

        Assertions.assertTrue(
                status == 200 || status == 201,
                "Expected 200 or 201 on update consumable, but got: " + status
        );

        JsonNode updated = consumablesClient.parseConsumable(response);

        Assertions.assertEquals(consumableId, updated.get("id").asText(), "Updated id must be the same");
        Assertions.assertEquals(body.get("name"), updated.get("name").asText(), "Updated name must match request");
        Assertions.assertEquals(body.get("itemNumber"), updated.get("itemNumber").asText(), "Updated itemNumber must match request");
        Assertions.assertEquals(
                ((Number) body.get("costForBusiness")).doubleValue(),
                updated.get("costForBusiness").asDouble(),
                0.0001,
                "Updated costForBusiness must match request"
        );
        Assertions.assertEquals(body.get("description"), updated.get("description").asText(), "Updated description must match request");

        Map<String, Object> muReq = (Map<String, Object>) body.get("measurementUnit");
        JsonNode muResp = updated.get("measurementUnit");
        Assertions.assertEquals(muReq.get("id"), muResp.get("id").asText(), "Updated measurementUnit.id must match request");

        List<Map<String, Object>> tagsReq = (List<Map<String, Object>>) body.get("tags");
        if (tagsReq != null && !tagsReq.isEmpty()) {
            JsonNode tagsResp = updated.get("tags");
            Assertions.assertTrue(tagsResp.isArray() && tagsResp.size() > 0, "Expected at least one tag in response");
            Assertions.assertEquals(
                    tagsReq.get(0).get("name"),
                    tagsResp.get(0).get("name").asText(),
                    "First tag name must match request"
            );
        }
    }

    // 3️⃣ PARTIAL UPDATE: PATCH /consumables/{id}
    @Test
    @Disabled("PATCH /consumables/{id} currently not implemented on backend (returns 405)")
    @Order(3)
    void partialUpdateConsumable_updatesSubsetOfFields() throws IOException {
        Assertions.assertNotNull(consumableId, "consumableId is null – previous tests probably failed");

        Map<String, Object> body = ConsumablesTestDataFactory.buildPartialUpdateConsumableBody(consumableId);

        APIResponse response = consumablesClient.partialUpdateConsumable(consumableId, body);
        int status = response.status();

        System.out.println("PATCH CONSUMABLE status: " + status);
        System.out.println("PATCH CONSUMABLE body: " + response.text());

        Assertions.assertTrue(
                status == 200 || status == 204,
                "Expected 200 or 204 on partialUpdate, but got: " + status
        );

        if (status == 200) {
            JsonNode patched = consumablesClient.parseConsumable(response);

            if (body.get("description") != null) {
                Assertions.assertEquals(body.get("description"), patched.get("description").asText(), "Patched description must match request");
            }
            if (body.get("costForBusiness") != null) {
                Assertions.assertEquals(
                        ((Number) body.get("costForBusiness")).doubleValue(),
                        patched.get("costForBusiness").asDouble(),
                        0.0001,
                        "Patched costForBusiness must match request"
                );
            }
        }
    }

    // 4️⃣ DELETE: DELETE /consumables (ids = [consumableId])
    @Test
    @Order(4)
    void deleteConsumable_deletesById() {
        Assertions.assertNotNull(consumableId, "consumableId is null – previous tests probably failed");

        APIResponse response = consumablesClient.deleteConsumables(Collections.singletonList(consumableId));
        int status = response.status();

        System.out.println("DELETE CONSUMABLE status: " + status);
        System.out.println("DELETE CONSUMABLE body: '" + response.text() + "'");

        Assertions.assertEquals(
                204,
                status,
                "Expected 204 No Content on delete consumable, but got: " + status
        );
    }
}
