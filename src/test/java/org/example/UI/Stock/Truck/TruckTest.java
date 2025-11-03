package org.example.UI.Stock.Truck;

import com.microsoft.playwright.assertions.PlaywrightAssertions;
import net.datafaker.Faker;
import org.assertj.core.api.Assertions;
import org.example.BaseUITestExtension.PlaywrightUiLoginBaseTest;
import org.example.UI.Models.Truck;
import org.example.UI.PageObjectModels.Alerts.AlertUtils;
import org.example.UI.PageObjectModels.Stock.StockPage;
import org.example.UI.PageObjectModels.Stock.Trucks.TruckFormPopUpPage;
import org.example.UI.PageObjectModels.Stock.Trucks.TrucksListPage;
import org.junit.jupiter.api.*;

import java.util.Random;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TruckTest extends PlaywrightUiLoginBaseTest {
    StockPage stockPage;
    TrucksListPage trucksListPage;
    TruckFormPopUpPage truckFormPopUpPage;

    Faker faker = new Faker();

    Truck truck = new Truck(
            "Truck-" + new Random().nextInt(100000),
            faker.bothify("???-####"),
            faker.vehicle().make(),
            faker.vehicle().model()
    );

    Truck editedTruck = new Truck(
            "Truck-" + new Random().nextInt(100000),
            faker.bothify("???-####"),
            faker.vehicle().make(),
            faker.vehicle().model()
    );

    @BeforeAll
    public void setUp() {
        openPath("/stock");
        stockPage = new StockPage(page);
        trucksListPage = new TrucksListPage(page);
    }

    @DisplayName("Create Truck Test")
    @Order(0)
    @Test
    public void createTruckTest() {
        stockPage.clickOnTruckTabButton();

        trucksListPage.waitForLoaded();
        truckFormPopUpPage = trucksListPage.clickOnAddTruckButton();
        truckFormPopUpPage.waitForLoaded();

        truckFormPopUpPage.setTruckName(truck.name);
        truckFormPopUpPage.setTruckPlate(truck.plate);
        truckFormPopUpPage.setTruckMake(truck.make);
        truckFormPopUpPage.setTruckModel(truck.model);
        truckFormPopUpPage.clickAddTruckButton();

        AlertUtils.waitForAlertVisible(page);
        Assertions.assertThat(AlertUtils.getAlertText(page)).isEqualTo("Location has been created");
        AlertUtils.waitForAlertHidden(page);

        PlaywrightAssertions.assertThat(trucksListPage.root())
                .containsText(truck.name + " - " + truck.make + " - " + truck.model);
    }

    @DisplayName("Update Truck Test")
    @Order(1)
    @Test
    public void updateTruckTest() {
        stockPage.clickOnTruckTabButton();

        trucksListPage.clickOnTruckThreeDotsButton();
        truckFormPopUpPage = trucksListPage.clickOnEditTruckButton();
        truckFormPopUpPage.waitForLoaded();

        truckFormPopUpPage.setTruckName(editedTruck.name);
        truckFormPopUpPage.setTruckPlate(editedTruck.plate);
        truckFormPopUpPage.setTruckMake(editedTruck.make);
        truckFormPopUpPage.setTruckModel(editedTruck.model);
        truckFormPopUpPage.clickSaveChangesButton();

        AlertUtils.waitForAlertVisible(page);
        Assertions.assertThat(AlertUtils.getAlertText(page)).isEqualTo("Location successfully updated");
        AlertUtils.waitForAlertHidden(page);

        PlaywrightAssertions.assertThat(trucksListPage.root())
                .containsText(editedTruck.name + " - " + editedTruck.make + " - " + editedTruck.model);
    }

    @DisplayName("Delete Truck Test")
    @Order(2)
    @Test
    public void deleteTruckTest() {
        stockPage.clickOnTruckTabButton();

        // Отримуємо ім'я першої вантажівки зі списку
        String firstTruckName = trucksListPage.getTrucksNamesList().get(0);

        // Якщо це TruckMain — пропускаємо тест
        if ("TruckMain".equalsIgnoreCase(firstTruckName.trim())) {
            System.out.println("⚠️ TruckMain cannot be deleted, skipping test.");
            Assumptions.assumeTrue(false, "TruckMain — cannot be deleted");
        }

        // Якщо не TruckMain — виконуємо видалення
        trucksListPage.clickOnTruckThreeDotsButton();
        trucksListPage.clickOnDeleteButton();
        trucksListPage.confirmDeleteInModal();

        AlertUtils.waitForAlertVisible(page);
        String alert = AlertUtils.getAlertText(page);
        Assertions.assertThat(alert).contains("deleted successfully");
        AlertUtils.waitForAlertHidden(page);

        // Перевірка, що вантажівка зникла зі списку
        Assertions.assertThat(trucksListPage.getTrucksNamesList()).doesNotContain(firstTruckName);
    }
}
