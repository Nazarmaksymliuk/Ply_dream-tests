package org.example.UI.PageObjectModels.Jobs;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.util.List;


public class ActiveJobsPage {
    private final Locator activeJobsTitle;
    private final Page page;

    private final Locator addJobButton;
    private final Locator jobNames;
    private final Locator clientsPhoneNumber;
    private final Locator startDateField;
    private final Locator alertMessage;
    private final Locator threeDotsButton;
    private final Locator editJobButton;
    private final Locator finishJobButton;
    private final Locator setAsFinishedButton;
    private final Locator finishedJobsTab;


    public ActiveJobsPage(Page page) {
        this.page = page;
        activeJobsTitle = page.getByText("Active Jobs");
        addJobButton = page.getByText("Add Job");
        jobNames = page.locator(".link_black.fw_600");
        clientsPhoneNumber = page.locator("[data-field='clientPhoneNumber'][role='cell']");
        startDateField = page.locator("[data-field='scheduledAt'][role='cell']");
        alertMessage = page.locator("[role='alert']");
        threeDotsButton = page.getByTestId("MoreHorizIcon").first();
        editJobButton = page.getByRole(AriaRole.MENUITEM, new Page.GetByRoleOptions().setName("Edit"));;
        finishJobButton = page.getByRole(AriaRole.MENUITEM, new Page.GetByRoleOptions().setName("Finish Job"));
        setAsFinishedButton = page.getByText("Set as finished");
        finishedJobsTab = page.getByTestId("FlagIcon");
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

    public List<String> getJobList(){
        return jobNames.allInnerTexts();
    }
    public Locator getJobListLocator(){
        return jobNames.first();
    }

    public String getFirstJobName(){
        return jobNames.first().allInnerTexts().getFirst();
    }

    public String getFirstClientsPhoneNumber(){
        return clientsPhoneNumber.allInnerTexts().getFirst();
    }

    public String getFirstStartDate(){
        return startDateField.allInnerTexts().getFirst();
    }

    public String getAlertMessage(){
        return alertMessage.innerText();
    }

    public Locator getAlertMessageLocator(){
        return alertMessage;
    }

    public void clickThreeDotsButton(){
        threeDotsButton.click();
    }

    public void finishJobButton(){
        finishJobButton.click();
    }

    public void setAsFinishedConfirmationModalButton(){
        setAsFinishedButton.click();
    }

    public FinishedJobsPage navigateToFinishedJobsPage(){
        finishedJobsTab.click();
        return new FinishedJobsPage(page);
    }

    public GeneralInfoJobPage clickEditJobButton(){
        editJobButton.click();
        return new GeneralInfoJobPage(page);
    }

    public void waitForJobVisible(String jobName) {
        page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName(jobName))
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public void waitForJobsToLoad() {
        jobNames.first().waitFor(
                new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(60000)
        );
    }


}