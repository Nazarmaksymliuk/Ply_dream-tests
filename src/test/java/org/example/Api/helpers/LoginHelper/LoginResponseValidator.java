package org.example.Api.helpers.LoginHelper;

import com.microsoft.playwright.APIResponse;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;

public class LoginResponseValidator {

    public static void expectValid200(APIResponse response, LoginClient client) throws IOException {
        int status = response.status();
        Assertions.assertEquals(200, status, "Status code must be 200");

        LoginResponse body = client.parseLoginResponse(response);

        Assertions.assertNotNull(body.getToken(), "token must not be null");
        Assertions.assertNotNull(body.getRefreshToken(), "refreshToken must not be null");
        Assertions.assertNotNull(body.getRole(), "role must not be null");
        Assertions.assertNotNull(body.getExpiresAt(), "expiresAt must not be null");

        Assertions.assertFalse(body.getExpiresAt().isEmpty(), "expiresAt must be non-empty");
    }
}
