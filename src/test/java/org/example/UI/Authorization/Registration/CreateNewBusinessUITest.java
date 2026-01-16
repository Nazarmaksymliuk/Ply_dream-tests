package org.example.UI.Authorization.Registration;

import com.mailslurp.apis.InboxControllerApi;
import com.mailslurp.apis.WaitForControllerApi;
import com.mailslurp.clients.ApiClient;
import com.mailslurp.models.Email;
import com.mailslurp.models.InboxDto;
import com.microsoft.playwright.Response;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Api.helpers.RegistrationHelper.RegistrationClient;
import org.example.BaseUIApiExtension.PlaywrightUiApiBaseTest;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.example.UI.PageObjectModels.Authorization.Registration.SignUpAccountPage;
import org.example.UI.PageObjectModels.Authorization.Registration.SignUpEmailPage;
import org.example.UI.PageObjectModels.Authorization.Registration.SignUpPage;
import org.example.UI.PageObjectModels.Authorization.Registration.SignUpVerifyPage;
import org.example.UI.PageObjectModels.Dashboard.DashboardPage;
import org.junit.jupiter.api.*;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CreateNewBusinessUITest extends PlaywrightUiApiBaseTest {

    private static final Pattern OTP_PATTERN = Pattern.compile("🔐\\s*(\\d{6})");
    private static final String API_KEY = "sk_IpYmWCns7XrA0Hce_69ApcsQfppgQn9i5GAR87le1VrqtsW5xz7VoGhJgb0Caxr2N8abNG8ouNSpq0y93";

    private RegistrationClient adminRegistrationClient;
    private String createdBusinessId;

    private static final ObjectMapper OM = new ObjectMapper();

    @BeforeEach
    void setup() {
        openPath("/sign-in");
        adminRegistrationClient = new RegistrationClient(adminApi); // як у твоєму API тесті
    }

    @AfterEach
    void cleanup() {
        if (createdBusinessId != null) {
            var resp = adminRegistrationClient.deleteBusinessAsAdmin(createdBusinessId);
            Assertions.assertTrue(resp.status() == 204 || resp.status() == 200);
        }
    }

    @Test
    @Order(0)
    @DisplayName("Create new Business - Sign Up flow with OTP (MailSlurp)")
    void createNewBusiness_success() throws Exception {

        // ==== MailSlurp init ====
        String apiKey = System.getenv("MAILSLURP_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = API_KEY;
        }


        ApiClient apiClient = new ApiClient();
        apiClient.setApiKey(apiKey);

        InboxControllerApi inboxApi = new InboxControllerApi(apiClient);
        InboxDto inbox = inboxApi.createInboxWithDefaults().execute();
        String emailAddress = inbox.getEmailAddress();

        // ==== Test data ====
        String firstName = "AQA-FirstName-" + new Random().nextInt(100000);
        String lastName = "AQA-LastName-" + new Random().nextInt(100000);
        String businessName = "AQA-Business-" + new Random().nextInt(100000);
        String password = "Test+1234";

        // ==== UI flow ====
        SignUpPage signUpPage = new SignUpPage(page);

        SignUpEmailPage emailPage = signUpPage
                .openSignUp()
                .chooseRegisterNewBusiness()
                .continueToEmailStep();

        SignUpAccountPage accountPage = emailPage
                .setEmail(emailAddress)
                .acceptTerms()
                .clickGetStarted();

        Response registerResp = page.waitForResponse(
                r -> r.url().startsWith("https://dev-api.getply.com")
                        && r.request().method().equals("POST")
                        && r.url().contains("/register")
                        && (r.status() == 200 || r.status() == 201),
                () -> {
                    accountPage
                            .setFirstName(firstName)
                            .setLastName(lastName)
                            .setBusinessName(businessName)
                            .setPassword(password)
                            .setConfirmPassword(password)
                            .continueToVerify();
                }
        );
        SignUpVerifyPage verifyPage = new SignUpVerifyPage(page);


// дістаємо businessId
        String body = registerResp.text();
        JsonNode json = OM.readTree(body);
        createdBusinessId = json.path("businessId").asText(null);

        Assertions.assertNotNull(
                createdBusinessId,
                "businessId not found in register response: " + body
        );


        // ==== Wait OTP email ====
        WaitForControllerApi waitApi = new WaitForControllerApi(apiClient);
        Email latest = waitApi.waitForLatestEmail()
                .inboxId(inbox.getId())
                .timeout(TimeUnit.MINUTES.toMillis(2))
                .unreadOnly(true)
                .execute();

        String otpCode = extractOtp(latest.getBody());

        // ==== Verify step ====
        DashboardPage dashboardPage = verifyPage
                .setOtpCode(otpCode)
                .chooseBusinessType()
                .clickContinue()
                .clickContinue()
                .clickGetStarted();

        dashboardPage.assertLoaded();
        dashboardPage.assertDeleteSampleDataPresent();

    }

    @Test
    @Order(1)
    @DisplayName("Verify user cannot create business with existing email")
    void cannotCreateBusiness_withExistingEmail() {

        String existingEmail = "maksimlukoleg56@gmail.com"; // email, який вже існує

        SignUpPage signUpPage = new SignUpPage(page);

        SignUpEmailPage emailPage = signUpPage
                .openSignUp()
                .chooseRegisterNewBusiness()
                .continueToEmailStep();

        emailPage
                .setEmail(existingEmail)
                .acceptTerms()
                .clickGetStarted();

        // ==== Assertion ====
        // Перевіряємо, що зʼявилось повідомлення про існуючий email
        assertThat(page.getByText("Email already exists")).isVisible();
    }


    private static String extractOtp(String body) {
        if (body == null) throw new RuntimeException("Email body is null");
        Matcher m = OTP_PATTERN.matcher(body);
        if (!m.find()) throw new RuntimeException("Verification code not found in email body");
        return m.group(1);
    }
}
