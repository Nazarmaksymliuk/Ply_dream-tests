package org.example.UI.Dashboard;

import org.example.BaseUITestExtension.PlaywrightUiLoginBaseTest;
import org.example.UI.PageObjectModels.Dashboard.DashboardPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DashboardTest extends PlaywrightUiLoginBaseTest {
    DashboardPage dashboardPage;
    @BeforeEach
    public void setUp() {
        openPath("/dashboard");
        dashboardPage = new DashboardPage(page);
    }

    @Test
    void dashboardHeaderVisible() {
        var header = dashboardPage.getDashboard();
        assertTrue(header.isVisible());
    }
}
