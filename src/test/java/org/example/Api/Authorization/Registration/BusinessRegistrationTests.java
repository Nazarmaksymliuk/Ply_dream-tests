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

    private RegistrationClient registrationClient;

    private String businessId;
    private Map<String, Object> lastRequestBody;

    @BeforeAll
    void initClient() {
        registrationClient = new RegistrationClient(apiRequest);
    }

    // 1️⃣ POST /register – реєстрація бізнесу
    //@Test
    @Order(1)
    void registerBusiness_createsNewBusiness() throws IOException {
        Map<String, Object> body = buildRegisterBody();
        lastRequestBody = body;

        APIResponse response = registrationClient.registerBusiness(body);
        int status = response.status();

        System.out.println("REGISTER status: " + status);
        System.out.println("REGISTER body: " + response.text());

        // swagger: 201 Created, але вказано також 200 OK
        Assertions.assertTrue(
                status == 201 || status == 200,
                "Expected 201 or 200 on register, but got: " + status
        );

        JsonNode json = registrationClient.parseRegistrationResponse(response);

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

    // ---------- helpers ----------

    private Map<String, Object> buildRegisterBody() {
        Map<String, Object> body = new HashMap<>();

        long ts = System.currentTimeMillis();

        body.put("businessLegalName", "API Test Business " + ts);

        // ⚠️ за потреби заміни домен на той, який бекенд дозволяє для тестів
        body.put("email", "aqa.api+" + ts + "@example.com");

        body.put("firstName", "API");
        body.put("lastName", "Tester");

        // якщо валидація пароля жорстка – підлаштуй під правила бекенда
        body.put("password", "Qwerty123!");

        return body;
    }
}
