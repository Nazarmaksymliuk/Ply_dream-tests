package org.example.UI.Dashboard;

import org.example.BaseTestExtension.PlaywrightBaseTest;
import org.example.PageObjectModels.Dashboard.DashboardPage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DashboardTest extends PlaywrightBaseTest {
    DashboardPage dashboardPage;
    @BeforeEach
    public void setUp() {
        openPath("/dashboard"); //NOT necessary cause we open the browser in the dashboard
        dashboardPage = new DashboardPage(page);
    }

    @Test
    void dashboardHeaderVisible() {
        var header = dashboardPage.getDashboard();
        assertTrue(header.isVisible());
    }
}
