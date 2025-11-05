package org.example.UI.PageObjectModels.Dashboard;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class DashboardPage {
    private final Page page;
    // Локатори
    private final Locator dashboard;

    public DashboardPage(Page page) {
        this.page = page;
        dashboard = page.getByText("Dashboard").first();
        //dashboard = page.locator(".header_dashboard_title");
    }

    public Locator getDashboard() {
        return dashboard;
    }
}
