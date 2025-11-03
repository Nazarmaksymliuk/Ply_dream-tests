package org.example.UI.Suppliers;

import com.microsoft.playwright.assertions.PlaywrightAssertions;
import net.datafaker.Faker;
import org.assertj.core.api.Assertions;
import org.example.BaseUITestExtension.PlaywrightUiLoginBaseTest;
import org.example.UI.Models.Supplier;
import org.example.UI.PageObjectModels.Alerts.AlertUtils;
import org.example.UI.PageObjectModels.Suppliers.SupplierPopUpPage;
import org.example.UI.PageObjectModels.Suppliers.SuppliersPage;
import org.junit.jupiter.api.*;

import java.util.Random;
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SuppliersTest extends PlaywrightUiLoginBaseTest {
    SuppliersPage suppliersPage;
    SupplierPopUpPage supplierPopUpPage;

    @BeforeEach
    public void setUp() {
        openPath("/suppliers");
        suppliersPage = new SuppliersPage(page);
    }

    Faker faker = new Faker();
    Supplier expectedSupplier = new Supplier(
            "Supplier-AQA-" + new Random().nextInt(100000),   // name
            faker.address().city(),              // city
            "Fast delivery every Monday note", // note
            faker.name().fullName(),                 // contactName
            faker.internet().emailAddress(),    // contactEmail
            "1234567890"           // contactPhone
    );

    @DisplayName("Create Supplier Test")
    @Order(0)
    @Test
    public void createSupplierTest() {
        PlaywrightAssertions.assertThat(suppliersPage.addSupplierButtonLocator()).isVisible();

        supplierPopUpPage = suppliersPage.clickAddSupplierButton();

        supplierPopUpPage.setSupplierName(expectedSupplier.name);
        supplierPopUpPage.setSupplierCity(expectedSupplier.city);
        supplierPopUpPage.setSupplierNote(expectedSupplier.note);
        supplierPopUpPage.setSupplierContactName(expectedSupplier.contactName);
        supplierPopUpPage.setSupplierContactEmail(expectedSupplier.contactEmail);
        supplierPopUpPage.setSupplierContactPhone(expectedSupplier.contactPhone);

        supplierPopUpPage.clickSaveButton();

        AlertUtils.waitForAlertVisible(page);
        String alert = AlertUtils.getAlertText(page);
        Assertions.assertThat(alert).isEqualTo("Supplier successfully added");
        AlertUtils.waitForAlertHidden(page);

        waitForElementPresent(expectedSupplier.name);
        Assertions.assertThat(suppliersPage.getFirstSupplierName()).isEqualTo(expectedSupplier.name);
        Assertions.assertThat(suppliersPage.getFirstContactName()).isEqualTo(expectedSupplier.contactName);
        Assertions.assertThat(suppliersPage.getFirstSupplierEmail()).isEqualTo(expectedSupplier.contactEmail);
        Assertions.assertThat(suppliersPage.getFirstSupplierPhone()).isEqualTo("+1" + expectedSupplier.contactPhone);
        Assertions.assertThat(suppliersPage.getFirstSupplierCity()).isEqualTo(expectedSupplier.city);

    }

    Supplier editedSupplier = new Supplier(
            "Supplier-AQA-edited-" + new Random().nextInt(100000),   // name
            faker.address().city() + " " + "edited",              // city
            "Fast delivery every Monday note edited", // note
            faker.name().fullName() + " " + "edited" ,
            // contactName
            faker.internet().emailAddress() + "edited",    // contactEmail
            "0987654321"           // contactPhone
    );
    @DisplayName("Update Supplier Test")
    @Order(1)
    @Test
    public void updateSupplierTest() {
        PlaywrightAssertions.assertThat(suppliersPage.addSupplierButtonLocator()).isVisible();

        suppliersPage.clickOnThreeDotsButton();
        supplierPopUpPage = suppliersPage.clickEditSupplierMenuButton();

        supplierPopUpPage.setSupplierName(editedSupplier.name);
        supplierPopUpPage.setSupplierCity(editedSupplier.city);
        supplierPopUpPage.setSupplierNote(editedSupplier.note);
        supplierPopUpPage.setSupplierContactName(editedSupplier.contactName);
        supplierPopUpPage.setSupplierContactEmail(editedSupplier.contactEmail);
        supplierPopUpPage.setSupplierContactPhone(editedSupplier.contactPhone);

        supplierPopUpPage.clickSaveButton();

        AlertUtils.waitForAlertVisible(page);
        String alert = AlertUtils.getAlertText(page);
        Assertions.assertThat(alert).isEqualTo("Supplier successfully updated");
        AlertUtils.waitForAlertHidden(page);

        waitForElementPresent(editedSupplier.name);
        Assertions.assertThat(suppliersPage.getFirstSupplierName()).isEqualTo(editedSupplier.name);
        Assertions.assertThat(suppliersPage.getFirstContactName()).isEqualTo(editedSupplier.contactName);
        Assertions.assertThat(suppliersPage.getFirstSupplierEmail()).isEqualTo(editedSupplier.contactEmail);
        Assertions.assertThat(suppliersPage.getFirstSupplierPhone()).isEqualTo("+1" + editedSupplier.contactPhone);
        Assertions.assertThat(suppliersPage.getFirstSupplierCity()).isEqualTo(editedSupplier.city);

    }

    @DisplayName("Delete Supplier Test")
    @Order(2)
    @Test
    public void deleteSupplierTest() {
        PlaywrightAssertions.assertThat(suppliersPage.addSupplierButtonLocator()).isVisible();

        String supplierName = suppliersPage.getFirstSupplierName();

        suppliersPage.clickOnThreeDotsButton();
        suppliersPage.clickDeleteSupplierMenuButton();
        suppliersPage.clickDeleteSupplierInConfirmationModalButton();

        AlertUtils.waitForAlertVisible(page);
        String alert = AlertUtils.getAlertText(page);
        Assertions.assertThat(alert).isEqualTo("Supplier successfully deleted");
        AlertUtils.waitForAlertHidden(page);


        waitForElementRemoved(supplierName);
        Assertions.assertThat(suppliersPage.getSuppliersList()).doesNotContain(supplierName);
    }

}
