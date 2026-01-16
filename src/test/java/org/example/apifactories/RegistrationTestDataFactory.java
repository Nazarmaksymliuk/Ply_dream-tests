package org.example.apifactories;

import java.util.HashMap;
import java.util.Map;

public class RegistrationTestDataFactory {

    public static Map<String, Object> buildAdminRegisterBody(String suffix) {
        Map<String, Object> body = new HashMap<>();

        long ts = System.currentTimeMillis();

        body.put("firstName", "Admin");
        body.put("lastName", "Created-" + suffix);
        body.put("email", "admin.created+" + ts + "@example.com");
        body.put("businessLegalName", "Admin Business " + suffix + " " + ts);

        return body;
    }
}
