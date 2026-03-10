package org.example.routes;

public final class Routes {
    private Routes() {}

    public static final String BASE_URL = System.getProperty(
            "baseUrl",
            "https://dev.getply.com"
    );

    public static final String BASE_API_URL = System.getProperty(
            "baseApiUrl",
            "https://dev-api.getply.com"
    );

    public static final String LOGIN = "/sign-in";
    public static final String DASHBOARD = "/dashboard";
    public static final String WAREHOUSE = "/warehouse";
    public static final String TRUCKS = "/trucks";
    public static final String JOBS = "/jobs";

    public static String url(String path) {
        return BASE_URL + path;
    }

    public static String api_url(String path) {
        return BASE_API_URL + path;
    }
}
