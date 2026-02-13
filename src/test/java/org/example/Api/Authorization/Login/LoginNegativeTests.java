package org.example.Api.Authorization.Login;

import com.microsoft.playwright.APIResponse;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.example.Api.helpers.LoginHelper.LoginClient;
import org.example.BaseAPITestExtension.BaseApiTest;
import org.example.creds.Users;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Epic("Authorization")
@Feature("Login Negative Scenarios")
public class LoginNegativeTests extends BaseApiTest {

    private LoginClient loginClient;

    @BeforeAll
    void initClient() {
        loginClient = new LoginClient(userApi);
    }

    @DisplayName("Wrong password returns 401")
    @Test
    void wrongPassword_returnsError() {
        APIResponse response = loginClient.login(
                Users.ADMIN.email(),
                "WrongPassword"
        );

        Assertions.assertEquals(401, response.status(),
                "Expected 401 for wrong password but got: " + response.status());
    }

    @DisplayName("Non-existing user returns 401")
    @Test
    void nonExistingUser_returnsError() {
        APIResponse response = loginClient.login(
                Users.INVALID_USER.email(),
                Users.INVALID_USER.password()
        );

        Assertions.assertEquals(401, response.status(),
                "Expected 401 for non-existing user but got: " + response.status());
    }

    @DisplayName("Empty email returns 400")
    @Test
    void emptyEmail_returnsValidationError() {
        APIResponse response = loginClient.login(
                "",
                Users.ADMIN.password()
        );

        Assertions.assertEquals(400, response.status(),
                "Expected 400 for empty email but got: " + response.status());
    }

    @DisplayName("Empty password returns 400")
    @Test
    void emptyPassword_returnsValidationError() {
        APIResponse response = loginClient.login(
                Users.ADMIN.email(),
                ""
        );

        Assertions.assertEquals(400, response.status(),
                "Expected 400 for empty password but got: " + response.status());
    }
}
