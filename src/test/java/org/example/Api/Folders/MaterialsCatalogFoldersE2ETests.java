package org.example.Api.Folders;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.APIResponse;
import org.example.Api.helpers.FoldersHelper.FoldersClient;
import org.example.BaseAPITestExtension.BaseApiTest;
import org.example.apifactories.FoldersTestDataFactory;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.Map;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MaterialsCatalogFoldersE2ETests extends BaseApiTest {

    private FoldersClient foldersClient;
    private String folderId;

    @BeforeAll
    void initClient() {
        foldersClient = new FoldersClient(userApi);
    }

    // 1️⃣ CREATE FOLDER
    @Test
    @Order(1)
    void createFolder_createsAndStoresId() throws IOException {
        Map<String, Object> body =
                FoldersTestDataFactory.buildCreateFolderBody("API Folder ");

        APIResponse response = foldersClient.createFolder(body);
        int status = response.status();

        System.out.println("CREATE FOLDER status: " + status);
        System.out.println("CREATE FOLDER body: " + response.text());

        Assertions.assertTrue(
                status == 201 || status == 200,
                "Expected 201 or 200 on createFolder, but got: " + status
        );

        folderId = foldersClient.extractFolderId(response);
        Assertions.assertNotNull(folderId, "folderId must not be null after create");
        Assertions.assertFalse(folderId.isEmpty(), "folderId must not be empty");

        JsonNode created = foldersClient.parseFolder(response);

        Assertions.assertEquals(
                body.get("name"),
                created.get("name").asText(),
                "Created folder name must match request"
        );

        Assertions.assertEquals(0, created.get("countKits").asInt());
        Assertions.assertEquals(0, created.get("countMaterials").asInt());
        Assertions.assertEquals(0, created.get("countToolUnits").asInt());
    }

    // 2️⃣ UPDATE FOLDER
    @Test
    @Order(2)
    void updateFolder_updatesName() throws IOException {
        Assertions.assertNotNull(folderId, "folderId is null – create test probably failed");

        Map<String, Object> body =
                FoldersTestDataFactory.buildUpdateFolderBody(folderId, "API Folder UPDATED ");

        APIResponse response = foldersClient.updateFolder(folderId, body);
        int status = response.status();

        System.out.println("UPDATE FOLDER status: " + status);
        System.out.println("UPDATE FOLDER body: " + response.text());

        Assertions.assertTrue(
                status == 200 || status == 201,
                "Expected 200 or 201 on updateFolder, but got: " + status
        );

        JsonNode updated = foldersClient.parseFolder(response);

        Assertions.assertEquals(folderId, updated.get("id").asText(), "Updated id must be the same");
        Assertions.assertEquals(
                body.get("name"),
                updated.get("name").asText(),
                "Updated folder name must match request"
        );

        if (body.get("parentId") != null) {
            Assertions.assertEquals(
                    body.get("parentId"),
                    updated.get("parentId").asText(),
                    "Updated parentId must match request"
            );
        }
    }

    // 3️⃣ DELETE FOLDER
    @Test
    @Order(3)
    void deleteFolder_deletesPreviouslyCreated() {
        Assertions.assertNotNull(folderId, "folderId is null – create test probably failed");

        APIResponse response = foldersClient.deleteFolder(
                folderId,
                false, // transferFolder
                null   // newParentId
        );

        int status = response.status();

        System.out.println("DELETE FOLDER status: " + status);
        System.out.println("DELETE FOLDER body: '" + response.text() + "'");

        Assertions.assertEquals(
                204,
                status,
                "Expected 204 on deleteFolder, but got: " + status
        );
    }
}
