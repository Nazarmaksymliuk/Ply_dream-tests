package org.example.Api.Materials;

import com.microsoft.playwright.APIResponse;
import org.example.Api.helpers.MaterialsHelper.MaterialsClient;
import org.example.BaseAPITestExtension.BaseApiTest;
import org.example.apifactories.MaterialsTestDataFactory;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MaterialNegativeTests extends BaseApiTest {

    private MaterialsClient materialsClient;
    private final List<String> createdIds = new ArrayList<>();

    @BeforeAll
    void initClient() {
        materialsClient = new MaterialsClient(userApi);
    }

    @AfterAll
    void cleanup() {
        for (String id : createdIds) {
            try { materialsClient.deleteMaterial(id); } catch (Exception ignored) {}
        }
    }

    @Test
    @Order(1)
    void duplicates_byName_shouldReturn400AndMessage() throws IOException {
        long ts = System.currentTimeMillis();
        String name = "DUP-NAME-" + ts;
        String itemNumber = "DUP-NAME-ITEM-" + ts;

        // create material
        Map<String, Object> createBody = MaterialsTestDataFactory.buildCreateMaterialRequest("", "");
        createBody.put("name", name);
        createBody.put("itemNumber", itemNumber);

        APIResponse createResp = materialsClient.createMaterial(createBody);
        Assertions.assertTrue(createResp.status() == 200 || createResp.status() == 201);

        String createdId = materialsClient.extractMaterialId(createResp);
        createdIds.add(createdId);

        // check duplicates (same name)
        Map<String, Object> dupReq = MaterialsTestDataFactory.buildDuplicatesRequest(
                name, null, Collections.emptyList()
        );

        APIResponse dupResp = materialsClient.checkDuplicates(dupReq);

        System.out.println("DUPLICATES name status: " + dupResp.status());
        System.out.println("DUPLICATES name body: " + dupResp.text());

        Assertions.assertEquals(400, dupResp.status(), "Expected 400 BAD_REQUEST for duplicate name");

        List<String> msgs = materialsClient.extractErrorMessages(dupResp);
        Assertions.assertFalse(msgs.isEmpty(), "Expected error message array to be non-empty");

        String joined = String.join("\n", msgs);
        Assertions.assertTrue(
                joined.contains("Name already exists"),
                "Expected message to mention duplicate Name, but got: " + joined
        );
    }

    @Test
    @Order(2)
    void duplicates_byItemNumber_shouldReturn400AndMessage() throws IOException {
        long ts = System.currentTimeMillis();
        String name = "DUP-ITEM-NAME-" + ts;
        String itemNumber = "DUP-ITEM-" + ts;

        // create material
        Map<String, Object> createBody = MaterialsTestDataFactory.buildCreateMaterialRequest("", "");
        createBody.put("name", name);
        createBody.put("itemNumber", itemNumber);

        APIResponse createResp = materialsClient.createMaterial(createBody);
        Assertions.assertTrue(createResp.status() == 200 || createResp.status() == 201);

        String createdId = materialsClient.extractMaterialId(createResp);
        createdIds.add(createdId);

        // check duplicates (same itemNumber)
        Map<String, Object> dupReq = MaterialsTestDataFactory.buildDuplicatesRequest(
                null, itemNumber, Collections.emptyList()
        );

        APIResponse dupResp = materialsClient.checkDuplicates(dupReq);

        System.out.println("DUPLICATES itemNumber status: " + dupResp.status());
        System.out.println("DUPLICATES itemNumber body: " + dupResp.text());

        Assertions.assertEquals(400, dupResp.status(), "Expected 400 BAD_REQUEST for duplicate itemNumber");

        List<String> msgs = materialsClient.extractErrorMessages(dupResp);
        Assertions.assertFalse(msgs.isEmpty(), "Expected error message array to be non-empty");

        String joined = String.join("\n", msgs);

        // якщо текст інший (Item Number already exists) — заміниш цей contains
        Assertions.assertTrue(
                joined.toLowerCase().contains("already exists"),
                "Expected message to mention duplicate, but got: " + joined
        );
    }
}