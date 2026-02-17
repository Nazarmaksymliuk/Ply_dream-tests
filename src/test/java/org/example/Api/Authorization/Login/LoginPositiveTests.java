package org.example.Api.Authorization.Login;

import com.microsoft.playwright.APIResponse;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.example.Api.helpers.LoginHelper.LoginClient;
import org.example.Api.helpers.LoginHelper.LoginResponseValidator;
import org.example.BaseAPITestExtension.BaseApiTest;
import org.example.creds.Users;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.example.config.TestEnvironment;
import org.junit.jupiter.api.Timeout;

@Epic("Authorization")
@Feature("Login Positive Scenarios")
@Timeout(value = TestEnvironment.E2E_TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
public class LoginPositiveTests extends BaseApiTest {

    private LoginClient loginClient;

    @BeforeAll
    void initClient() {
        loginClient = new LoginClient(userApi);
    }

    @DisplayName("Login for Admin User")
    @Test
    void successfulLogin_returns200AndValidBody() throws IOException {
        APIResponse response = loginClient.login(
                Users.ADMIN.email(),
                Users.ADMIN.password()
        );

        LoginResponseValidator.expectValid200(response, loginClient);
    }

    @DisplayName("Login for Tech Role")
    @Test
    void successfulLogin_techRole() {
        APIResponse response = loginClient.login(
                Users.TECH.email(),
                Users.TECH.password()
        );

        int status = response.status();
        if (status != 200 && status != 201) {
            throw new AssertionError("Expected status 200 or 201 but got: " + status);
        }
    }
}
