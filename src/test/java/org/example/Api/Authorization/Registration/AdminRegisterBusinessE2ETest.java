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
public class AdminRegisterBusinessE2ETest extends BaseApiTest {

    private RegistrationClient adminRegistrationClient;
    private String businessId;

    @BeforeAll
    void initClient() {
        //  важливо: adminApi з BaseApiTest
        adminRegistrationClient = new RegistrationClient(adminApi);
    }

    @Test
    @Order(1)
    @DisplayName("Admin can register business and delete it")
    void adminRegisterAndDeleteBusiness_success() throws IOException {

        // ---------- test data ----------
        long ts = System.currentTimeMillis();

        Map<String, Object> body = new HashMap<>();
        body.put("firstName", "Admin");
        body.put("lastName", "Tester");
        body.put("email", "admin.api+" + ts + "@example.com");
        body.put("businessLegalName", "Admin API Business " + ts);

        // ---------- CREATE ----------
        APIResponse createResp =
                adminRegistrationClient.registerBusinessByAdmin(body);

        int createStatus = createResp.status();

        System.out.println("ADMIN REGISTER status: " + createStatus);
        System.out.println("ADMIN REGISTER body: " + createResp.text());

        Assertions.assertTrue(
                createStatus == 200 || createStatus == 201,
                "Expected 200 or 201 on admin register, got: " + createStatus
        );

        businessId = adminRegistrationClient.extractBusinessId(createResp);
        Assertions.assertNotNull(businessId, "businessId must not be null after admin register");

        JsonNode created =
                adminRegistrationClient.parseRegistrationResponse(createResp);

        Assertions.assertEquals(body.get("email"), created.get("email").asText());
        Assertions.assertEquals(body.get("firstName"), created.get("firstName").asText());
        Assertions.assertEquals(body.get("lastName"), created.get("lastName").asText());

        // ---------- DELETE ----------
        APIResponse deleteResp =
                adminRegistrationClient.deleteBusinessAsAdmin(businessId);

        int deleteStatus = deleteResp.status();

        System.out.println("DELETE BUSINESS status: " + deleteStatus);
        System.out.println("DELETE BUSINESS body: '" + deleteResp.text() + "'");

        Assertions.assertTrue(
                deleteStatus == 200 || deleteStatus == 204,
                "Expected 200 or 204 on delete business, got: " + deleteStatus
        );
    }
}
