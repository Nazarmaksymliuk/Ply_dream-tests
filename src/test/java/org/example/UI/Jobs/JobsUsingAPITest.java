package org.example.UI.Jobs;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import org.assertj.core.api.Assertions;
import org.example.Api.helpers.LocationsHelper.LocationsClient;
import org.example.BaseUIApiExtension.PlaywrightUiApiBaseTest;
import org.example.UI.Models.ClientContacts;
import org.example.UI.Models.Job;
import org.example.UI.PageObjectModels.Jobs.ActiveJobsPage;
import org.example.UI.PageObjectModels.Jobs.ContactsJobPage;
import org.example.UI.PageObjectModels.Jobs.FinishedJobsPage;
import org.example.UI.PageObjectModels.Jobs.GeneralInfoJobPage;
import org.example.apifactories.TruckLocationsTestDataFactory;
import org.example.fixtures.TruckApiFixture;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Random;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JobsUsingAPITest extends PlaywrightUiApiBaseTest {

    private static LocationsClient locationsClient;
    private static String truckId;
    private static String truckName;

    ActiveJobsPage activeJobsPage;
    GeneralInfoJobPage generalInfoJobPage;
    ContactsJobPage contactsJobPage;
    FinishedJobsPage finishedJobsPage;

    // ✅ створюємо назви job тільки тут, але truckName беремо з API
    private final Job customJob = new Job(
            "AQA-Job-" + new Random().nextInt(100000),
            null // підставимо нижче
    );

    private final Job editedJob = new Job(
            "AQA-Job-edited-" + new Random().nextInt(100000),
            null // підставимо нижче
    );

    ClientContacts clientContacts = new ClientContacts(
            "Nazar-AQA",
            "1234567890",
            "This Note for the best person that reads that code"
    );

    ClientContacts editedClientContacts = new ClientContacts(
            "Nazar-AQA",
            "1234567890",
            "This Note for the best person that reads that code"
    );

    // =========================
    // ✅ API SETUP / CLEANUP
    // =========================
    private static TruckApiFixture truckFixture;

    @BeforeAll
    void createTruckViaApi() throws IOException {
        truckFixture = TruckApiFixture.create(userApi)
                .provisionTruck("UI-JOBS-API Truck ");

        truckId = truckFixture.truckId();
        truckName = truckFixture.truckName();
    }

    @AfterAll
    void deleteTruckViaApi() {
        if (truckFixture != null) {
            truckFixture.cleanup("Cleanup after JobsTest (truck created via API)");
        }
    }

    // =========================
    // UI SETUP
    // =========================
    @BeforeEach
    public void setUp() {
        openPath("/jobs");
        activeJobsPage = new ActiveJobsPage(page);

        // ✅ підставляємо API truck name в job-и
        customJob.truckName = truckName;
        editedJob.truckName = truckName;
    }

    // =========================
    // TESTS
    // =========================
    @DisplayName("Creating Job Test (uses Truck created via API)")
    @Order(0)
    @Test
    public void CreateJobTest() {
        PlaywrightAssertions.assertThat(activeJobsPage.getJobsTitle()).isVisible();
        PlaywrightAssertions.assertThat(activeJobsPage.getAddJobButton()).isEnabled();

        generalInfoJobPage = activeJobsPage.clickAddJobButton();

        generalInfoJobPage.setJobName(customJob.jobName);

        // ✅ Truck з API
        generalInfoJobPage.setTruckToJob(customJob.truckName);

        generalInfoJobPage.clickOnSelectDateButton();
        generalInfoJobPage.clickTodayButton();

        contactsJobPage = generalInfoJobPage.clickContactsTabButton();
        contactsJobPage.setClientName(clientContacts.clientName);
        contactsJobPage.setClientPhoneNumber(clientContacts.clientPhone);
        contactsJobPage.setNoteField(clientContacts.clientNote);
        contactsJobPage.clickAddJobButton();

        PlaywrightAssertions.assertThat(activeJobsPage.getAlertMessageLocator()).isVisible();
        Assertions.assertThat(activeJobsPage.getAlertMessage())
                .isEqualTo("The job '%s' has been successfully added", customJob.jobName);

        PlaywrightAssertions.assertThat(activeJobsPage.getAlertMessageLocator())
                .isHidden(new LocatorAssertions.IsHiddenOptions().setTimeout(15000));

        activeJobsPage.waitForJobVisible(customJob.jobName);
        Assertions.assertThat(activeJobsPage.getFirstJobName()).isVisible();
        Assertions.assertThat(activeJobsPage.getFirstJobName()).isEqualTo(customJob.jobName);
        Assertions.assertThat(activeJobsPage.getJobList()).contains(customJob.jobName);
        Assertions.assertThat(activeJobsPage.getFirstClientsPhoneNumber())
                .isEqualTo("+1" + clientContacts.clientPhone);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        LocalDate actualDate = LocalDate.parse(activeJobsPage.getFirstStartDate(), formatter);
        LocalDate today = LocalDate.now();
        Assertions.assertThat(actualDate)
                .as("Start date should be today's date")
                .isEqualTo(today);
    }

    @DisplayName("Update Job Test (uses same Truck created via API)")
    @Order(1)
    @Test
    public void UpdateJobTest() {
        PlaywrightAssertions.assertThat(activeJobsPage.getJobsTitle()).isVisible();
        PlaywrightAssertions.assertThat(activeJobsPage.getAddJobButton()).isEnabled();

        activeJobsPage.clickThreeDotsButton();
        generalInfoJobPage = activeJobsPage.clickEditJobButton();

        generalInfoJobPage.setJobName(editedJob.jobName);

        // ✅ Truck з API
        generalInfoJobPage.setTruckToJob(editedJob.truckName);

        contactsJobPage = generalInfoJobPage.clickContactsTabButton();
        contactsJobPage.setClientName(editedClientContacts.clientName);
        contactsJobPage.setClientPhoneNumber(editedClientContacts.clientPhone);
        contactsJobPage.setNoteField(editedClientContacts.clientNote);
        contactsJobPage.clickEditJobButton();

        PlaywrightAssertions.assertThat(activeJobsPage.getAlertMessageLocator()).isVisible();
        Assertions.assertThat(activeJobsPage.getAlertMessage())
                .isEqualTo("The job has been successfully updated");

        PlaywrightAssertions.assertThat(activeJobsPage.getAlertMessageLocator())
                .isHidden(new LocatorAssertions.IsHiddenOptions().setTimeout(15000));

        activeJobsPage.waitForJobVisible(editedJob.jobName);
        Assertions.assertThat(activeJobsPage.getFirstJobName()).isVisible();
        Assertions.assertThat(activeJobsPage.getFirstJobName()).isEqualTo(editedJob.jobName);
        Assertions.assertThat(activeJobsPage.getJobList()).contains(editedJob.jobName);
        Assertions.assertThat(activeJobsPage.getFirstClientsPhoneNumber())
                .isEqualTo("+1" + editedClientContacts.clientPhone);
    }

    @DisplayName("Finish Job Test")
    @Order(2)
    @Test
    public void FinishJobTest() {
        PlaywrightAssertions.assertThat(activeJobsPage.getJobsTitle())
                .isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(60000));

        activeJobsPage.waitForJobsToLoad();
        Assertions.assertThat(activeJobsPage.getFirstJobName()).isVisible();
        String jobForFinishName = activeJobsPage.getFirstJobName();

        if ("MainJob".equals(jobForFinishName)) {
            System.out.println("⚠️ MainJob could not be deleted");
            Assumptions.assumeTrue(false, "MainJob — could not be deleted");
        }

        activeJobsPage.clickThreeDotsButton();
        activeJobsPage.finishJobButton();
        activeJobsPage.setAsFinishedConfirmationModalButton();

        PlaywrightAssertions.assertThat(activeJobsPage.getAlertMessageLocator()).isVisible();
        Assertions.assertThat(activeJobsPage.getAlertMessage()).isEqualTo("Job is successfully completed");
        PlaywrightAssertions.assertThat(activeJobsPage.getAlertMessageLocator())
                .isHidden(new LocatorAssertions.IsHiddenOptions().setTimeout(15000));

        PlaywrightAssertions.assertThat(activeJobsPage.getJobListLocator()).isVisible();
        Assertions.assertThat(activeJobsPage.getJobList()).doesNotContain(jobForFinishName);

        finishedJobsPage = activeJobsPage.navigateToFinishedJobsPage();
        PlaywrightAssertions.assertThat(activeJobsPage.getJobListLocator()).isVisible();
        Assertions.assertThat(finishedJobsPage.getJobList()).contains(jobForFinishName);
    }
}
