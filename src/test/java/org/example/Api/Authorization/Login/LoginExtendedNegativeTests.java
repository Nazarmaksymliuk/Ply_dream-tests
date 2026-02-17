package org.example.Api.Authorization.Login;

import com.microsoft.playwright.APIResponse;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.example.Api.helpers.LoginHelper.LoginClient;
import org.example.Api.helpers.assertions.ApiAssertions;
import org.example.BaseAPITestExtension.BaseApiTest;
import org.example.config.TestEnvironment;
import org.example.creds.Users;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Extended negative tests for login functionality.
 * Tests additional edge cases beyond what LoginNegativeTests covers.
 * Covers security scenarios like SQL injection, XSS, and boundary testing.
 */
@Epic("Authorization")
@Feature("Login Extended Negative Scenarios")
@Timeout(value = TestEnvironment.E2E_TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
public class LoginExtendedNegativeTests extends BaseApiTest {

    private static final Logger log = LoggerFactory.getLogger(LoginExtendedNegativeTests.class);

    private LoginClient loginClient;

    @BeforeAll
    void initClient() {
        loginClient = new LoginClient(userApi);
        log.info("LoginClient initialized for extended negative tests");
    }

    @DisplayName("Login with SQL injection in email returns 400 or 404")
    @Test
    void login_withSqlInjectionInEmail_returns400or404() {
        String sqlInjectionEmail = "admin@test.com' OR '1'='1";
        log.info("Testing SQL injection attempt in email: {}", sqlInjectionEmail);

        APIResponse response = loginClient.login(
                sqlInjectionEmail,
                Users.ADMIN.password()
        );

        ApiAssertions.assertStatusOneOf(
                response,
                "SQL injection in email should be rejected",
                400, 404
        );
        log.info("SQL injection attempt properly rejected with status: {}", response.status());
    }

    @DisplayName("Login with XSS in email returns 400 or 404")
    @Test
    void login_withXssInEmail_returns400or404() {
        String xssEmail = "<script>alert('x')</script>@test.com";
        log.info("Testing XSS attempt in email: {}", xssEmail);

        APIResponse response = loginClient.login(
                xssEmail,
                Users.ADMIN.password()
        );

        ApiAssertions.assertStatusOneOf(
                response,
                "XSS in email should be rejected",
                400, 404
        );
        log.info("XSS attempt properly rejected with status: {}", response.status());
    }

    @DisplayName("Login with very long email returns 400")
    @Test
    void login_withVeryLongEmail_returns400() {
        String veryLongEmail = "a".repeat(10000) + "@test.com";
        log.info("Testing very long email (length: {})", veryLongEmail.length());

        APIResponse response = loginClient.login(
                veryLongEmail,
                Users.ADMIN.password()
        );

        ApiAssertions.assertStatus(
                400,
                response,
                "Very long email should be rejected with 400"
        );
        log.info("Very long email properly rejected with status: {}", response.status());
    }

    @DisplayName("Login with very long password returns 400 or 404")
    @Test
    void login_withVeryLongPassword_returns400or404() {
        String veryLongPassword = "x".repeat(10000);
        log.info("Testing very long password (length: {})", veryLongPassword.length());

        APIResponse response = loginClient.login(
                Users.ADMIN.email(),
                veryLongPassword
        );

        ApiAssertions.assertStatusOneOf(
                response,
                "Very long password should be rejected",
                400, 404
        );
        log.info("Very long password properly rejected with status: {}", response.status());
    }

    @DisplayName("Login with null email returns 400")
    @Test
    void login_withNullEmail_returns400() {
        log.info("Testing null email");

        APIResponse response = loginClient.login(
                null,
                Users.ADMIN.password()
        );

        ApiAssertions.assertStatus(
                400,
                response,
                "Null email should be rejected with 400"
        );
        log.info("Null email properly rejected with status: {}", response.status());
    }

    @DisplayName("Login with special character password returns 404")
    @Test
    void login_withSpecialCharPassword_returns404() {
        String specialCharPassword = "!@#$%^&*()";
        log.info("Testing special character password: {}", specialCharPassword);

        APIResponse response = loginClient.login(
                Users.ADMIN.email(),
                specialCharPassword
        );

        ApiAssertions.assertStatus(
                404,
                response,
                "Wrong password with special characters should return 404"
        );
        log.info("Special character password properly rejected with status: {}", response.status());
    }
}
