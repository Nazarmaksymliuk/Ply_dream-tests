package org.example.UI.PageObjectModels.Jobs;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.List;

public class FinishedJobsPage {
    private final Page page;
    private final Locator jobNames;

    public FinishedJobsPage(Page page) {
        this.page = page;
        jobNames = page.locator(".link_black.fw_600");
    }

    public List<String> getJobList(){
        return jobNames.allInnerTexts();
    }


}
