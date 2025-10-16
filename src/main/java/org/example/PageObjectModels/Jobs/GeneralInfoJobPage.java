package org.example.PageObjectModels.Jobs;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

public class GeneralInfoJobPage {
    private final Page page;
    private final Locator jobName;
    private final Locator selectProject;
    private final Locator selectTruck;
    private final Locator selectInputTruck;
    private final Locator assignEmployees;
    private final Locator selectDate;
    private final Locator todayButton;
    private final Locator contactsTabButton;

    public GeneralInfoJobPage(Page page) {
        this.page = page;
        this.jobName = page.getByPlaceholder("Enter job name");
        this.selectProject = page.locator("#react-select-41-placeholder");
        this.selectTruck = page.locator(".react_select__input-container").nth(1);

        this.selectInputTruck = page.locator("input[role='combobox']").nth(3);

        this.assignEmployees = page.locator("#react-select-43-placeholder");
        this.selectDate = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Enter start date"));
        this.todayButton = page.locator(".react-datepicker__day--today");
        contactsTabButton = page.getByText("Contacts");

    }

    public void setJobName(String jobNameValue) {
        jobName.fill(jobNameValue);
    }

    public void setTruckToJob(String truckToJobValue) {
//        selectTruck.click();
//        selectInputTruck.fill(truckToJobValue);
//        selectTruck.waitFor(new Locator.WaitForOptions().setTimeout(2000));
//        selectTruck.press("Enter");
        // 1) клік по потрібному селекту (контейнеру з плейсхолдером)
        selectTruck.click();

        // 2) працюємо саме з ІНПУТОМ (а не контейнером!)
        selectInputTruck.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        selectInputTruck.fill("");                 // на всяк випадок очистити
        selectInputTruck.type(truckToJobValue);    // type краще для react-select

        // 3) дочекайся, що комбобокс відкрився і з’явились опції
        Locator openCombo = page.locator("input[role='combobox'][aria-expanded='true']");
        openCombo.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        page.waitForSelector(".react_select__menu");       // меню
        page.waitForSelector(".react_select__option");     // хоча б одна опція

        // 4) тепер безпечно підтверджувати вибір — Enter САМЕ В ІНПУТІ
        openCombo.press("Enter");
    }


    public void clickOnSelectDateButton() {
        selectDate.click();
    }

    public void clickTodayButton() {
        todayButton.click();
    }

    public ContactsJobPage clickContactsTabButton() {
        contactsTabButton.click();
        return new ContactsJobPage(page);
    }



}
