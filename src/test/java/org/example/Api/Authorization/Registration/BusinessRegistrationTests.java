package org.example.Api.Authorization.Registration;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.APIResponse;
import org.example.Api.helpers.RegistrationHelper.RegistrationClient;
import org.example.BaseAPITestExtension.BaseApiTest;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BusinessRegistrationTests extends BaseApiTest {

    private RegistrationClient userRegistrationClient;
    private RegistrationClient adminRegistrationClient;

    private String businessId;
    private Map<String, Object> lastRequestBody;

    @BeforeAll
    void initClients() {
        // контекст з userApi (можна взагалі без токена, але так теж ок)
        userRegistrationClient = new RegistrationClient(userApi);
        // контекст з adminApi – тут уже є Authorization: Bearer <adminToken>
        adminRegistrationClient = new RegistrationClient(adminApi);
    }

    // 1️⃣ POST /register – реєстрація бізнесу
    //@Test
    @Order(1)
    void registerBusiness_createsNewBusiness() throws IOException {
        Map<String, Object> body = buildRegisterBody();
        lastRequestBody = body;

        APIResponse response = userRegistrationClient.registerBusiness(body);
        int status = response.status();

        System.out.println("REGISTER status: " + status);
        System.out.println("REGISTER body: " + response.text());

        // swagger: 201 Created, але вказано також 200 OK
        Assertions.assertTrue(
                status == 201 || status == 200,
                "Expected 201 or 200 on register, but got: " + status
        );

        JsonNode json = userRegistrationClient.parseRegistrationResponse(response);

        // businessId
        JsonNode businessIdNode = json.get("businessId");
        Assertions.assertNotNull(businessIdNode, "businessId must be present in response");
        Assertions.assertFalse(businessIdNode.asText().isEmpty(), "businessId must not be empty");
        businessId = businessIdNode.asText();

        // email / firstName / lastName мають збігатися з тим, що ми відправляли
        Assertions.assertEquals(
                body.get("email"),
                json.get("email").asText(),
                "Email in response must match request"
        );
        Assertions.assertEquals(
                body.get("firstName"),
                json.get("firstName").asText(),
                "firstName in response must match request"
        );
        Assertions.assertEquals(
                body.get("lastName"),
                json.get("lastName").asText(),
                "lastName in response must match request"
        );

        // опціонально: переконатися, що пароль не повертається
        Assertions.assertNull(
                json.get("password"),
                "Password field should not be present in response"
        );
    }

    // 2️⃣ DELETE /businesses/admin/{businessId} – видалення бізнесу як адмін
    //@Test
    @Order(2)
    void deleteBusiness_asAdmin_deletesPreviouslyRegisteredBusiness() {
        Assertions.assertNotNull(businessId, "businessId is null – registration test probably failed");

        APIResponse response = adminRegistrationClient.deleteBusinessAsAdmin(businessId);
        int status = response.status();

        System.out.println("DELETE BUSINESS status: " + status);
        System.out.println("DELETE BUSINESS body: '" + response.text() + "'");

        // swagger: 204 No Content, але допускаємо й 200
        Assertions.assertTrue(
                status == 204 || status == 200,
                "Expected 204 or 200 on deleteBusiness, but got: " + status
        );

        String body = response.text().trim();
        // часто бекенд повертає пусте тіло на 204
        Assertions.assertTrue(
                body.isEmpty() || "{}".equals(body),
                "Expected empty body or {}, but got: '" + body + "'"
        );
    }

    // ---------- helpers ----------

    private Map<String, Object> buildRegisterBody() {
        Map<String, Object> body = new HashMap<>();

        long ts = System.currentTimeMillis();

        body.put("businessLegalName", "API Test Business " + ts);
        body.put("email", "aqa.api+" + ts + "@example.com");
        body.put("firstName", "API");
        body.put("lastName", "Tester");
        body.put("password", "Qwerty123!"); // якщо будуть правила по паролю – підлаштуємо

        return body;
    }
}
