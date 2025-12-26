package org.example.apifactories;

import java.util.HashMap;
import java.util.Map;

public class FoldersTestDataFactory {

    private FoldersTestDataFactory() {
        // utility class
    }

    // POST body – створюємо root folder (без parentId)
    public static Map<String, Object> buildCreateFolderBody(String namePrefix) {
        Map<String, Object> body = new HashMap<>();

        body.put("name", namePrefix + System.currentTimeMillis());
        // id не передаємо – нехай генерується на бекенді
        // parentId не передаємо – root folder

        return body;
    }

    // PUT body – оновлюємо тільки name (і опційно parentId, якщо треба перемістити)
    public static Map<String, Object> buildUpdateFolderBody(String folderId, String namePrefix) {
        Map<String, Object> body = new HashMap<>();

        body.put("id", folderId);
        body.put("name", namePrefix + System.currentTimeMillis());

        return body;
    }

    // якщо колись знадобиться move – можеш юзати цей
    public static Map<String, Object> buildUpdateFolderBody(String folderId, String namePrefix, String parentId) {
        Map<String, Object> body = buildUpdateFolderBody(folderId, namePrefix);
        if (parentId != null) {
            body.put("parentId", parentId);
        }
        return body;
    }
}
