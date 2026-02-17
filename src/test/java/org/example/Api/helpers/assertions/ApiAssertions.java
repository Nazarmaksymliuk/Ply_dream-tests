package org.example.Api.helpers.assertions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIResponse;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;

/**
 * Reusable assertion helpers for API tests.
 * Eliminates duplicated assertion patterns across test classes.
 */
public final class ApiAssertions {

    private static final ObjectMapper OM = new ObjectMapper();

    private ApiAssertions() {}

    /**
     * Assert that the response has the expected status code.
     */
    public static void assertStatus(int expected, APIResponse response, String context) {
        Assertions.assertEquals(expected, response.status(),
                context + " — expected status " + expected + " but got " + response.status()
                        + ", body: " + response.text());
    }

    /**
     * Assert that the response status is one of the expected codes.
     */
    public static void assertStatusOneOf(APIResponse response, String context, int... expectedCodes) {
        int actual = response.status();
        for (int code : expectedCodes) {
            if (actual == code) return;
        }
        StringBuilder sb = new StringBuilder(context)
                .append(" — expected one of [");
        for (int i = 0; i < expectedCodes.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(expectedCodes[i]);
        }
        sb.append("] but got ").append(actual).append(", body: ").append(response.text());
        Assertions.fail(sb.toString());
    }

    /**
     * Assert that a JSON field is a non-null, non-empty string.
     */
    public static String assertNonEmptyString(JsonNode node, String fieldName, String context) {
        Assertions.assertNotNull(node, context + " — JSON node is null");
        JsonNode fieldNode = node.get(fieldName);
        Assertions.assertNotNull(fieldNode, context + " — field '" + fieldName + "' is missing");
        Assertions.assertTrue(fieldNode.isTextual(),
                context + " — field '" + fieldName + "' is not a string, got: " + fieldNode.getNodeType());
        String value = fieldNode.asText();
        Assertions.assertFalse(value.isEmpty(),
                context + " — field '" + fieldName + "' is empty");
        return value;
    }

    /**
     * Assert that a JSON field matches the expected value.
     */
    public static void assertFieldEquals(Object expected, JsonNode node, String fieldName, String context) {
        Assertions.assertNotNull(node, context + " — JSON node is null");
        JsonNode fieldNode = node.get(fieldName);
        Assertions.assertNotNull(fieldNode, context + " — field '" + fieldName + "' is missing");

        if (expected instanceof Number) {
            Assertions.assertEquals(
                    ((Number) expected).doubleValue(),
                    fieldNode.asDouble(),
                    0.0001,
                    context + " — field '" + fieldName + "' mismatch"
            );
        } else {
            Assertions.assertEquals(
                    String.valueOf(expected),
                    fieldNode.asText(),
                    context + " — field '" + fieldName + "' mismatch"
            );
        }
    }

    /**
     * Assert response is successful (200 or 201) and parse body as JSON.
     */
    public static JsonNode assertSuccessAndParse(APIResponse response, String context) throws IOException {
        assertStatusOneOf(response, context, 200, 201);
        return OM.readTree(response.text());
    }

    /**
     * Extract id from response and assert it is non-null and non-empty.
     */
    public static String assertIdPresent(JsonNode node, String context) {
        return assertNonEmptyString(node, "id", context);
    }

    /**
     * Assert that a JSON array field is non-empty.
     */
    public static void assertArrayNotEmpty(JsonNode node, String fieldName, String context) {
        Assertions.assertNotNull(node, context + " — JSON node is null");
        JsonNode arr = node.get(fieldName);
        Assertions.assertNotNull(arr, context + " — field '" + fieldName + "' is missing");
        Assertions.assertTrue(arr.isArray(), context + " — field '" + fieldName + "' is not an array");
        Assertions.assertFalse(arr.isEmpty(), context + " — field '" + fieldName + "' is empty array");
    }

    /**
     * Assert that response has error status and response body contains expected message substring.
     */
    public static void assertErrorContains(APIResponse response, int expectedStatus, String messageSubstring, String context) throws IOException {
        assertStatus(expectedStatus, response, context);
        String body = response.text();
        JsonNode root = OM.readTree(body);

        String message = "";
        if (root.has("message")) {
            JsonNode msgNode = root.get("message");
            if (msgNode.isArray()) {
                StringBuilder sb = new StringBuilder();
                for (JsonNode n : msgNode) sb.append(n.asText()).append(" ");
                message = sb.toString();
            } else {
                message = msgNode.asText();
            }
        } else {
            message = body;
        }

        Assertions.assertTrue(
                message.toLowerCase().contains(messageSubstring.toLowerCase()),
                context + " — expected error message to contain '" + messageSubstring + "' but got: " + message
        );
    }
}
