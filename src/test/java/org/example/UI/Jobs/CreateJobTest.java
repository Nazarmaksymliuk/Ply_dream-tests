package org.example.UI.Jobs;

import com.microsoft.playwright.assertions.PlaywrightAssertions;
import org.example.BaseTestExtension.PlaywrightBaseTest;
import org.example.Models.ClientContacts;
import org.example.Models.Job;
import org.example.PageObjectModels.Jobs.ActiveJobsPage;
import org.example.PageObjectModels.Jobs.ContactsJobPage;
import org.example.PageObjectModels.Jobs.GeneralInfoJobPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class CreateJobTest extends PlaywrightBaseTest {
    ActiveJobsPage activeJobsPage;
    GeneralInfoJobPage generalInfoJobPage;
    ContactsJobPage contactsJobPage;


    @BeforeEach
    public void setUp() {
        openPath("/jobs");
        activeJobsPage = new ActiveJobsPage(page);

    }


    Job customJob = new Job("NazarAQA",
            "1234567890",
            "This note for the best person in ma Life",
            "AQA-Job-" + new Random().nextInt(100000),
            "MainTruck"
    );
    ClientContacts clientContacts = new ClientContacts(
            "Nazar-AQA",
            "1234567890",
            "This Note for the best person that reads that code"
    );



    @Test
    public void testCreateJobTest() {
        PlaywrightAssertions.assertThat(activeJobsPage.getJobsTitle()).isVisible();
        PlaywrightAssertions.assertThat(activeJobsPage.getAddJobButton()).isEnabled();

        generalInfoJobPage = activeJobsPage.clickAddJobButton();


        generalInfoJobPage.setJobName(customJob.jobName);
        generalInfoJobPage.setTruckToJob(customJob.truckName);
        generalInfoJobPage.clickOnSelectDateButton();
        generalInfoJobPage.clickTodayButton();


        contactsJobPage = generalInfoJobPage.clickContactsTabButton();
        contactsJobPage.setClientName(clientContacts.clientName);
        contactsJobPage.setClientPhoneNumber(clientContacts.clientPhone);
        contactsJobPage.setNoteField(clientContacts.clientNote);
        contactsJobPage.clickAddJobButton();



    }

}
