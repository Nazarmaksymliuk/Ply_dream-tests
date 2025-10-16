package org.example.PageObjectModels.Jobs;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;


public class ActiveJobsPage {
    private final Locator activeJobsTitle;
    private final Page page;

    private final Locator addJobButton;


    public ActiveJobsPage(Page page) {
        this.page = page;
        activeJobsTitle = page.getByText("Active Jobs");
        addJobButton = page.getByText("Add Job");

    }

    public Locator getJobsTitle() {
        return activeJobsTitle;
    }
    public Locator getAddJobButton() {
        return addJobButton;
    }

    public GeneralInfoJobPage clickAddJobButton() {
        addJobButton.click();
        return new GeneralInfoJobPage(page);
    }



}
