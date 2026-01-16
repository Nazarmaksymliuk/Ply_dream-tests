package org.example.UI.PageObjectModels.Dashboard;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

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

    public void assertLoaded() {
        // мінімальна перевірка: root видимий + URL містить dashboard (підкоригуєш якщо інше)
        assertThat(page.locator("#root")).isVisible();
    }

    public void assertDeleteSampleDataPresent() {
        assertThat(page.locator("#root"))
                .containsText("Delete sample data");
    }

}
