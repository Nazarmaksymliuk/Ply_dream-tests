package org.example.Api.Authorization.Login;

import com.microsoft.playwright.APIResponse;
import org.example.Api.helpers.LoginHelper.LoginClient;
import org.example.Api.helpers.LoginHelper.LoginResponseValidator;
import org.example.BaseAPITestExtension.BaseApiTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class LoginPositiveTests extends BaseApiTest {

    private LoginClient loginClient;

    @BeforeAll
    void initClient() {
        loginClient = new LoginClient(apiRequest);
    }

    @Test
    void successfulLogin_returns200AndValidBody() throws IOException {
        // TODO: реальні креденшіали тестового юзера
        APIResponse response = loginClient.login(
                "maksimlukoleg56@gmail.com",
                "Test+1234"
        );

        LoginResponseValidator.expectValid200(response, loginClient);
    }

    @Test
    void successfulLogin_mayReturn201IfBackendSoConfigured() {
        APIResponse response = loginClient.login(
                "kityby@forexzig.com",
                "Test+1234"
        );

        int status = response.status();
        if (status != 200 && status != 201) {
            throw new AssertionError("Expected status 200 or 201 but got: " + status);
        }
    }
}
