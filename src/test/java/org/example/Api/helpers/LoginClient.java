package org.example.Api.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LoginClient {

    private final APIRequestContext request;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LoginClient(APIRequestContext request) {
        this.request = request;
    }

    public APIResponse login(String email, String password) {
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        return request.post(
                "/v3/login",                         // 🔹 без "url:"
                RequestOptions.create().setData(body)
        );
    }

    public LoginResponse parseLoginResponse(APIResponse response) throws IOException {
        String json = response.text();
        return objectMapper.readValue(json, LoginResponse.class);
    }
}
