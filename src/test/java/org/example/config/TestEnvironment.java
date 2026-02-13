package org.example.config;

/**
 * Centralized test environment configuration.
 * All values can be overridden via system properties or environment variables.
 */
public final class TestEnvironment {

    private TestEnvironment() {}

    // === Timeouts (ms) ===
    public static final int DEFAULT_TIMEOUT_MS = 50_000;
    public static final int NAVIGATION_TIMEOUT_MS = 60_000;
    public static final int ELEMENT_WAIT_TIMEOUT_MS = 15_000;
    public static final int HEALTH_CHECK_TIMEOUT_MS = 20_000;

    // === Location IDs ===
    public static final String WAREHOUSE_MAIN_ID = System.getProperty(
            "warehouseMainId",
            System.getenv().getOrDefault("PLY_WAREHOUSE_MAIN_ID", "ac1f56fd-9919-137e-8199-1f504b6607e8")
    );
}
