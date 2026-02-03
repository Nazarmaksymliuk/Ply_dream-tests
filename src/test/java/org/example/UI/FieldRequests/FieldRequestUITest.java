package org.example.UI.FieldRequests;

import org.example.BaseUIApiExtension.PlaywrightUiApiBaseTest;
import org.example.UI.PageObjectModels.FieldRequests.FieldRequestsPage;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.Random;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FieldRequestUITest extends PlaywrightUiApiBaseTest {
    FieldRequestsPage fr;

    @BeforeEach
    public void setUp() {
        openPath("/field-requests");
        fr = new FieldRequestsPage(page);
    }

    String fieldRequestName = "AQA-FR-" + new Random().nextInt(100000);
    String editedFieldRequestName = "Edited-AQA-FR-" + new Random().nextInt(100000);

    @Test
    @Order(0)
    @DisplayName("Creating FR")
    void createFieldRequest_warehouse_success() {

        // create field request
        fr.startCreate()
                .selectLocation("Warehouse")
                .continueNext()
                .setName(fieldRequestName)
                .chooseDate(LocalDate.now())   // або конкретно LocalDate.of(2026, 1, 8)
                .setNotes("test note")
                .continueNext()
                .addFirstMaterialFromSearch()
                .setQuantityAndSave("10");

        fr.assertFieldRequestListed(fieldRequestName);

    }

    @Test
    @Order(1)
    @DisplayName("Updating FR")
    void updateFieldRequest_warehouse_success() {
        fr.openEditMenuOption()
                .setEditedName(editedFieldRequestName)
                .setEditedNotes("test note edited" + editedFieldRequestName)
                .saveChanges();

        fr.assertFieldRequestListed(editedFieldRequestName);

    }

    @Test
    @Order(2)
    @DisplayName("Deleting FR")
    void deleteFieldRequest_warehouse_success() {
        fr.openDeleteAndConfirmMenuOption();
        fr.assertFieldRequestNotListed(editedFieldRequestName);

    }
    

}
