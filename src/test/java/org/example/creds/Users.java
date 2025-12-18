package org.example.creds;

import org.example.creds.TestUser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Users {
    private static final Properties props = new Properties();

    static {
        try (InputStream is = Users.class.getClassLoader()
                .getResourceAsStream("config/users.properties")) {
            if (is != null) {
                props.load(is);
            } else {
                throw new RuntimeException("users.properties not found");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load users.properties", e);
        }
    }

    public static final TestUser ADMIN = new TestUser(
            props.getProperty("admin.email"),
            props.getProperty("admin.password")
    );

    public static final TestUser TECH = new TestUser(
            props.getProperty("tech.email"),
            props.getProperty("tech.password")
    );

    public static final TestUser MFAUser = new TestUser(
            props.getProperty("mfaUser.email"),
            props.getProperty("mfaUser.password")
    );

    public static final TestUser INVALID_USER = new TestUser(
            props.getProperty("invalid.email"),
            props.getProperty("invalid.password")
    );



}
