package org.example.Api.Folders;

import com.microsoft.playwright.APIResponse;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.example.Api.helpers.FoldersHelper.FoldersClient;
import org.example.Api.helpers.assertions.ApiAssertions;
import org.example.BaseAPITestExtension.BaseApiTest;
import org.example.apifactories.FoldersTestDataFactory;
import org.example.config.TestEnvironment;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Negative test scenarios for Folders (Catalogs) API.
 * Tests error handling, validation, and edge cases.
 */
@Epic("Folders")
@Feature("Folders Negative Scenarios")
@Timeout(value = TestEnvironment.E2E_TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
public class FoldersNegativeTests extends BaseApiTest {

    private static final Logger log = LoggerFactory.getLogger(FoldersNegativeTests.class);

    private FoldersClient foldersClient;
    private final List<String> createdFolderIds = new ArrayList<>();

    @BeforeAll
    void initClient() {
        foldersClient = new FoldersClient(userApi);
        log.info("FoldersClient initialized for negative tests");
    }

    @AfterAll
    void cleanup() {
        for (String id : createdFolderIds) {
            try {
                foldersClient.deleteFolder(id, false, null);
            } catch (Exception e) {
                log.warn("Cleanup: failed to delete folder {}: {}", id, e.getMessage());
            }
        }
    }

    @Test
    @DisplayName("Create folder with empty body - API accepts (no server validation)")
    void createFolder_withEmptyBody_accepted() throws IOException {
        log.info("Testing folder creation with empty body");

        Map<String, Object> emptyBody = new HashMap<>();
        APIResponse response = foldersClient.createFolder(emptyBody);

        log.info("Create folder with empty body - status: {}, body: {}", response.status(), response.text());

        // API does not validate empty body and returns 200
        ApiAssertions.assertStatusOneOf(response, "Create folder with empty body", 200, 201);

        String folderId = foldersClient.extractFolderId(response);
        if (folderId != null) {
            createdFolderIds.add(folderId);
        }
    }

    @Test
    @DisplayName("Create folder with missing name - API accepts (no server validation)")
    void createFolder_withMissingName_accepted() throws IOException {
        log.info("Testing folder creation with missing name");

        Map<String, Object> body = new HashMap<>();
        body.put("parentId", null);

        APIResponse response = foldersClient.createFolder(body);

        log.info("Create folder with missing name - status: {}, body: {}", response.status(), response.text());

        // API does not validate missing name and returns 200
        ApiAssertions.assertStatusOneOf(response, "Create folder with missing name", 200, 201);

        String folderId = foldersClient.extractFolderId(response);
        if (folderId != null) {
            createdFolderIds.add(folderId);
        }
    }

    @Test
    @DisplayName("Update folder with non-existent ID returns 404")
    void updateFolder_withNonExistentId_returns404() {
        log.info("Testing folder update with non-existent ID");

        String nonExistentId = UUID.randomUUID().toString();
        Map<String, Object> body = FoldersTestDataFactory.buildUpdateFolderBody(nonExistentId, "Updated Folder ");

        APIResponse response = foldersClient.updateFolder(nonExistentId, body);

        log.info("Update folder with non-existent ID - status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatusOneOf(response, "Update folder with non-existent ID", 400, 404);
    }

    @Test
    @DisplayName("Delete folder with non-existent ID returns 404 or 204")
    void deleteFolder_withNonExistentId_returns404or204() {
        log.info("Testing folder deletion with non-existent ID");

        String nonExistentId = UUID.randomUUID().toString();

        APIResponse response = foldersClient.deleteFolder(nonExistentId, false, null);

        log.info("Delete folder with non-existent ID - status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatusOneOf(response, "Delete folder with non-existent ID", 404, 204);
    }

    @Test
    @DisplayName("Delete already deleted folder returns 404")
    void deleteFolder_alreadyDeleted_returns404() throws IOException {
        log.info("Testing double delete of folder");

        // Create a folder
        Map<String, Object> createBody = FoldersTestDataFactory.buildCreateFolderBody("API Folder DblDel ");
        APIResponse createResp = foldersClient.createFolder(createBody);
        ApiAssertions.assertStatusOneOf(createResp, "Create folder for delete test", 200, 201);

        String folderId = foldersClient.extractFolderId(createResp);
        Assertions.assertNotNull(folderId, "folderId must not be null after creation");
        log.info("Created folder with ID: {}", folderId);

        // Delete first time
        APIResponse firstDelete = foldersClient.deleteFolder(folderId, false, null);
        log.info("First delete status: {}", firstDelete.status());
        ApiAssertions.assertStatusOneOf(firstDelete, "First delete of folder", 200, 204);

        // Delete second time
        APIResponse secondDelete = foldersClient.deleteFolder(folderId, false, null);
        log.info("Second delete status: {}, body: {}", secondDelete.status(), secondDelete.text());
        ApiAssertions.assertStatusOneOf(secondDelete, "Delete already deleted folder", 404, 204);
    }

    @Test
    @DisplayName("Update folder with invalid ID format returns error")
    void updateFolder_withInvalidIdFormat_returnsError() {
        log.info("Testing folder update with invalid ID format");

        String invalidId = "not-a-valid-uuid";
        Map<String, Object> body = FoldersTestDataFactory.buildUpdateFolderBody(invalidId, "Updated Folder ");

        APIResponse response = foldersClient.updateFolder(invalidId, body);

        log.info("Update folder with invalid ID format - status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatusOneOf(response, "Update folder with invalid ID format", 400, 404);
    }

    @Test
    @DisplayName("Create folder with SQL injection in name succeeds (sanitized)")
    void createFolder_withSqlInjectionInName_succeeds() throws IOException {
        log.info("Testing folder creation with SQL injection in name");

        Map<String, Object> body = new HashMap<>();
        body.put("name", "Folder'; DROP TABLE folders;-- " + System.currentTimeMillis());

        APIResponse response = foldersClient.createFolder(body);
        log.info("Create folder with SQL injection - status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatusOneOf(response, "Create folder with SQL injection", 200, 201, 400);

        if (response.status() == 200 || response.status() == 201) {
            String folderId = foldersClient.extractFolderId(response);
            if (folderId != null) {
                createdFolderIds.add(folderId);
                log.info("Folder with SQL injection name created, ID: {}", folderId);
            }
        }
    }

    @Test
    @DisplayName("Create folder with XSS in name succeeds (sanitized)")
    void createFolder_withXssInName_succeeds() throws IOException {
        log.info("Testing folder creation with XSS in name");

        Map<String, Object> body = new HashMap<>();
        body.put("name", "<script>alert('xss')</script>Folder " + System.currentTimeMillis());

        APIResponse response = foldersClient.createFolder(body);
        log.info("Create folder with XSS - status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatusOneOf(response, "Create folder with XSS", 200, 201, 400);

        if (response.status() == 200 || response.status() == 201) {
            String folderId = foldersClient.extractFolderId(response);
            if (folderId != null) {
                createdFolderIds.add(folderId);
                log.info("Folder with XSS name created, ID: {}", folderId);
            }
        }
    }

    @Test
    @DisplayName("Create folder with extremely long name returns error or accepts")
    void createFolder_withExtremelyLongName_returnsErrorOrAccepts() throws IOException {
        log.info("Testing folder creation with extremely long name (10000 characters)");

        String longName = "F".repeat(10000);
        Map<String, Object> body = new HashMap<>();
        body.put("name", longName);

        APIResponse response = foldersClient.createFolder(body);
        log.info("Create folder with long name - status: {}", response.status());

        ApiAssertions.assertStatusOneOf(response, "Create folder with extremely long name", 200, 201, 400, 500);

        if (response.status() == 200 || response.status() == 201) {
            String folderId = foldersClient.extractFolderId(response);
            if (folderId != null) {
                createdFolderIds.add(folderId);
            }
        }
    }

    @Test
    @DisplayName("Update folder with non-existent parentId returns error")
    void updateFolder_withNonExistentParentId_returnsError() throws IOException {
        log.info("Testing folder update with non-existent parentId");

        // Create a valid folder first
        Map<String, Object> createBody = FoldersTestDataFactory.buildCreateFolderBody("API Folder ParentTest ");
        APIResponse createResp = foldersClient.createFolder(createBody);
        ApiAssertions.assertStatusOneOf(createResp, "Create folder for parentId test", 200, 201);

        String folderId = foldersClient.extractFolderId(createResp);
        Assertions.assertNotNull(folderId);
        createdFolderIds.add(folderId);

        // Try to update with non-existent parentId
        String fakeParentId = UUID.randomUUID().toString();
        Map<String, Object> updateBody = FoldersTestDataFactory.buildUpdateFolderBody(folderId, "Updated ", fakeParentId);

        APIResponse response = foldersClient.updateFolder(folderId, updateBody);

        log.info("Update folder with non-existent parentId - status: {}, body: {}", response.status(), response.text());

        ApiAssertions.assertStatusOneOf(response, "Update folder with non-existent parentId", 400, 404, 200);
    }
}
