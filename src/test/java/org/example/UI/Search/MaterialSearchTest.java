package org.example.UI.Search;

import org.assertj.core.api.Assertions;
import org.example.BaseUITestExtension.PlaywrightUiLoginBaseTest;
import org.example.UI.PageObjectModels.Material.MaterialsListPage;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MaterialSearchTest extends PlaywrightUiLoginBaseTest {
    MaterialsListPage materialsListPage;

    // 👉 якщо опис/штрихкод видимі у гріді — підстав реальні значення нижче;
    // або заміни лоґікою, що дістає їх зі сторінки деталей матеріалу.
    String expectedName     = "UNIQUE-MATERIAL-SEARCH";
    String expectedItemNumber = "UNIQUE-ITEM#";
    String knownDescription = "UNIQUE MATERIAL DESCRIPTION";      // наприклад: "Sample description"
    String knownBarcode     = "53912859421";      // наприклад: "123456789012"

    @BeforeEach
    public void setUp() {
        openPath("/catalog");
        materialsListPage = new MaterialsListPage(page);
        materialsListPage.waitFirstRowVisible();
    }

    @DisplayName("Search material by item name, item#, description, barcode")
    @Order(0)
    @Test
    public void searchMaterialByVariousFields() {

        // 1) Пошук по item (назва)
        materialsListPage.searchByItem(expectedName);
        Assertions.assertThat(materialsListPage.isMaterialWithNamePresent(expectedName)).isTrue();

        // 2) Пошук по item# (код)
        materialsListPage.searchByItem(expectedItemNumber);
        Assertions.assertThat(materialsListPage.isMaterialWithNamePresent(expectedName)).isTrue();

        // 3) Пошук по description (якщо відомий термін)
        if (knownDescription != null && !knownDescription.isBlank()) {
            materialsListPage.searchByItem(knownDescription);
            Assertions.assertThat(materialsListPage.isMaterialWithNamePresent(expectedName)).isTrue();
        } else {
            System.out.println("ℹ️ knownDescription is empty — skipping description search step.");
        }

        // 4) Пошук по barcode (якщо відомий термін)
        if (knownBarcode != null && !knownBarcode.isBlank()) {
            materialsListPage.searchByItem(knownBarcode);
            Assertions.assertThat(materialsListPage.isMaterialWithNamePresent(expectedName)).isTrue();
        } else {
            System.out.println("ℹ️ knownBarcode is empty — skipping barcode search step.");
        }

        // очищення (за бажанням)
        materialsListPage.clearSearch();
    }

    @DisplayName("Search material in Warehouse location by name, item#, description, barcode")
    @Order(1)
    @Test
    public void searchMaterialInLocationByVariousFields() {
        openPath("/stock/warehouse/warehousemain/ac1f56fd-9919-137e-8199-1f504b6607e8");
        // 1) name
        materialsListPage.searchByItem(expectedName);
        Assertions.assertThat(materialsListPage.isMaterialWithNamePresent(expectedName)).isTrue();

        // 2) item#
        materialsListPage.searchByItem(expectedItemNumber);
        Assertions.assertThat(materialsListPage.isMaterialWithNamePresent(expectedName)).isTrue();

        // 3) description
        if (knownDescription != null && !knownDescription.isBlank()) {
            materialsListPage.searchByItem(knownDescription);
            Assertions.assertThat(materialsListPage.isMaterialWithNamePresent(expectedName)).isTrue();
        } else {
            System.out.println("ℹ️ knownDescription is empty — skipping description search step.");
        }

        // 4) barcode
        if (knownBarcode != null && !knownBarcode.isBlank()) {
            materialsListPage.searchByItem(knownBarcode);
            Assertions.assertThat(materialsListPage.isMaterialWithNamePresent(expectedName)).isTrue();
        } else {
            System.out.println("ℹ️ knownBarcode is empty — skipping barcode search step.");
        }

        // (опц.) очистити пошук на виході
        materialsListPage.clearSearch();
    }
}
