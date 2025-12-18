package org.example.routes;

public final class Routes {
    private Routes() {}

    public static final String BASE_URL = System.getProperty("baseUrl",
            "https://stage.getply.com");

    public static final String LOGIN = "/sign-in";
    public static final String DASHBOARD = "/dashboard";
    public static final String WAREHOUSE = "/warehouse";
    public static final String TRUCKS = "/trucks";
    public static final String JOBS = "/jobs";

    // Можеш додати метод, що будує повний URL
    public static String url(String path) {
        return BASE_URL + path;
    }
}
