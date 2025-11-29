package org.example.Api.Authorization.Login;

import com.microsoft.playwright.APIResponse;
import org.example.Api.helpers.LoginHelper.LoginClient;
import org.example.BaseAPITestExtension.BaseApiTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class LoginNegativeTests extends BaseApiTest {

    private LoginClient loginClient;

    @BeforeAll
    void initClient() {
        loginClient = new LoginClient(userApi);
    }

    @Test
    void wrongPassword_returnsError() {
        APIResponse response = loginClient.login(
                "maksimlukoleg56@gmail.com",
                "WrongPassword"
        );

        int status = response.status();

        Assertions.assertTrue(
                status == 400 || status == 401 || status == 404,
                "Expected 400, 401 or 404 but got: " + status
        );
    }

    @Test
    void nonExistingUser_returnsError() {
        APIResponse response = loginClient.login(
                "non_existing_user@example.com",
                "Test+1234"
        );

        int status = response.status();

        Assertions.assertTrue(
                status == 400 || status == 401 || status == 404,
                "Expected 400, 401 or 404 but got: " + status
        );
    }

    @Test
    void emptyEmail_returnsValidationError() {
        APIResponse response = loginClient.login(
                "",
                "Test+1234"
        );

        int status = response.status();

        Assertions.assertTrue(
                status == 400 || status == 401 || status == 404,
                "Expected 400, 401 or 404 but got: " + status
        );
    }

    @Test
    void emptyPassword_returnsValidationError() {
        APIResponse response = loginClient.login(
                "maksimlukoleg56@gmail.com",
                ""
        );

        int status = response.status();

        Assertions.assertTrue(
                status == 400 || status == 401 || status == 404,
                "Expected 400, 401 or 404 but got: " + status
        );
    }
}
