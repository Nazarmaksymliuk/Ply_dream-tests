package org.example.config;

/**
 * Centralized test environment configuration.
 * All values can be overridden via system properties or environment variables.
 */
public final class TestEnvironment {

    private TestEnvironment() {}

    // === Timeouts (ms) — for Playwright .setTimeout() ===
    public static final int DEFAULT_TIMEOUT_MS = 50_000;
    public static final int NAVIGATION_TIMEOUT_MS = 60_000;
    public static final int EXTENDED_TIMEOUT_MS = 30_000;
    public static final int DIALOG_TIMEOUT_MS = 20_000;
    public static final int HEALTH_CHECK_TIMEOUT_MS = 20_000;
    public static final int ELEMENT_WAIT_TIMEOUT_MS = 15_000;
    public static final int DROPDOWN_TIMEOUT_MS = 10_000;
    public static final int SHORT_TIMEOUT_MS = 5_000;

    // === Delays for waitForTimeout (ms) ===
    public static final int SEARCH_DELAY_MS = 2_500;
    public static final int DROPDOWN_DELAY_MS = 2_000;
    public static final int MEDIUM_DELAY_MS = 1_500;
    public static final int SMALL_DELAY_MS = 500;

    // === Location IDs ===
    public static final String WAREHOUSE_MAIN_ID = System.getProperty(
            "warehouseMainId",
            System.getenv().getOrDefault("PLY_WAREHOUSE_MAIN_ID", "ac1f56fd-9919-137e-8199-1f504b6607e8")
    );

    public static final String WAREHOUSE_TRANSFER_ID = System.getProperty(
            "warehouseTransferId",
            System.getenv().getOrDefault("PLY_WAREHOUSE_TRANSFER_ID", "ac1f56fd-9a4a-154f-819a-4c1fc3ea0711")
    );

    // === API Keys ===
    public static final String MAILSLURP_API_KEY = System.getProperty(
            "mailslurpApiKey",
            System.getenv().getOrDefault("MAILSLURP_API_KEY", "")
    );

    // === Test Timeouts (JUnit @Timeout, seconds) ===
    public static final int TEST_TIMEOUT_SECONDS = 120;
    public static final int E2E_TEST_TIMEOUT_SECONDS = 180;
}
