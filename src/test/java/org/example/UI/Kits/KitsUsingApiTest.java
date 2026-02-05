package org.example.UI.Kits;

import org.assertj.core.api.Assertions;
import org.example.Api.helpers.LocationsHelper.LocationsClient;
import org.example.BaseUIApiExtension.PlaywrightUiApiBaseTest;
import org.example.UI.Models.Kit;
import org.example.UI.PageObjectModels.Alerts.AlertUtils;
import org.example.UI.PageObjectModels.Catalog.CatalogPage;
import org.example.UI.PageObjectModels.Kits.KitsCreationFlow.KitGeneralInformationPage;
import org.example.UI.PageObjectModels.Kits.KitsCreationFlow.KitSettingsPage;
import org.example.UI.PageObjectModels.Kits.KitsCreationFlow.KitStockSetupPage;
import org.example.UI.PageObjectModels.Kits.KitsListPage;
import org.example.UI.PageObjectModels.Stock.Warehouse.WarehousePage;
import org.example.fixtures.WarehouseApiFixture;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.Random;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class KitsUsingApiTest extends PlaywrightUiApiBaseTest {

    private static LocationsClient locationsClient;
    private static String warehouseId;
    private static String warehouseName;

    KitGeneralInformationPage generalInformationPage;
    KitsListPage kitsListPage;
    CatalogPage catalogPage;
    KitStockSetupPage kitStockSetupPage;
    KitSettingsPage kitSettingsPage;
    WarehousePage warehousePage;

    private static WarehouseApiFixture warehouseFixture;

    // =========================
    // ✅ API SETUP / CLEANUP
    // =========================
    @BeforeAll
    void createWarehouseViaApi() throws IOException {
        warehouseFixture = WarehouseApiFixture.create(userApi)
                .provisionWarehouse("UI-CATALOG-E2E ");

        warehouseId = warehouseFixture.warehouseId();
        warehouseName = warehouseFixture.warehouseName();
    }

    @AfterAll
    static void deleteWarehouseViaApi() {
        if (warehouseFixture != null) {
            warehouseFixture.cleanup("Cleanup after MaterialInCatalogTest");
        }
    }

    // =========================
    // UI SETUP
    // =========================
    @BeforeEach
    public void setUp() {
        openPath("/catalog");
        catalogPage = new CatalogPage(page);
        kitsListPage = new KitsListPage(page);
        warehousePage = new WarehousePage(page);
    }

    // ✅ Важливо: location беремо з API
    Kit kit = new Kit(
            "Kit-" + new Random().nextInt(100000),
            "High-performance kit for any type of work.",
            "test tag",
            null
    );

    @DisplayName("Create Kit Test (Warehouse via API)")
    @Order(0)
    @Test
    public void testCreateKit() {
        kit.location = warehouseName;

        catalogPage.waitForLoaded();
        catalogPage.openKitsTab();

        generalInformationPage = catalogPage.clickAddItem(KitGeneralInformationPage.class);
        generalInformationPage.setKitName(kit.name);
        generalInformationPage.setDescription(kit.description);
        generalInformationPage.setTag(kit.tags);

        kitStockSetupPage = generalInformationPage.clickNext();
        kitStockSetupPage.clickAddLocation();

        kitStockSetupPage.clickGenerateCode();
        kitStockSetupPage.clickAddCode();

        // ✅ Динамічний warehouse з API
        kitStockSetupPage.setWarehouseUsingUtility(kit.location);

        kitSettingsPage = kitStockSetupPage.clickNext();
        kitSettingsPage.addFirstMaterialByName("Test");
        kitSettingsPage.setQtyForMaterialInKit(5);
        kitSettingsPage.addFirstToolByName("Test");
        Double kitPrice = kitSettingsPage.getTheKitPrice();

        kitSettingsPage.clickBottomSave();

        AlertUtils.waitForAlertVisible(page);
        String alert = AlertUtils.getAlertText(page);
        Assertions.assertThat(alert).isEqualTo("Kit \"%s\" has been successfully created", kit.name);
        AlertUtils.waitForAlertHidden(page);

        Assertions.assertThat(kitsListPage.getFirstKitNameInTheList()).isEqualTo(kit.name);
        Assertions.assertThat(kitsListPage.getFirstKitCostInTheListAsDouble()).isEqualTo(kitPrice);
    }

    Kit editedKit = new Kit(
            "Kit-edited" + new Random().nextInt(100000),
            "High-performance kit for any type of work-edited",
            "test tag-edited",
            null
    );

    @DisplayName("Update Kit Test")
    @Order(1)
    @Test
    public void testUpdateKit() {
        catalogPage.waitForLoaded();
        catalogPage.openKitsTab();

        catalogPage.openFirstRowKitThreeDots();

        generalInformationPage = catalogPage.chooseMenuEditKit();
        generalInformationPage.setKitName(editedKit.name);
        generalInformationPage.setDescription(editedKit.description);
        generalInformationPage.setTag(editedKit.tags);

        generalInformationPage.clickSave();

        AlertUtils.waitForAlertVisible(page);
        String alert = AlertUtils.getAlertText(page);
        Assertions.assertThat(alert).isEqualTo("Kit \"%s\" has been successfully updated", editedKit.name);
        AlertUtils.waitForAlertHidden(page);

        Assertions.assertThat(kitsListPage.getFirstKitNameInTheList()).isEqualTo(editedKit.name);
    }

    @DisplayName("Delete Kit Test")
    @Order(2)
    @Test
    public void testDeleteKit() {
        catalogPage.waitForLoaded();
        catalogPage.openKitsTab();

        String kitName = kitsListPage.getFirstKitNameInTheList();

        catalogPage.openFirstRowKitThreeDots();
        catalogPage.chooseMenuActionDelete();
        catalogPage.confirmDeleteItemInModal();

        AlertUtils.waitForAlertVisible(page);
        String alert = AlertUtils.getAlertText(page);
        Assertions.assertThat(alert).isEqualTo("Kit has been successfully deleted");
        AlertUtils.waitForAlertHidden(page);

        waitForElementRemoved(kitName);
        Assertions.assertThat(kitsListPage.getKitNamesList()).doesNotContain(kitName);
    }

    @DisplayName("Create Kit in Location Test")
    @Order(3)
    @Test
    public void testCreateKitInLocation() {
        openPath("/stock/warehouse/" + warehouseName.toLowerCase() + "/" + warehouseId);

        kit.location = warehouseName;

        warehousePage.waitForLoaded();
        warehousePage.clickOnKitsTabButton();
        generalInformationPage = warehousePage.clickOnAddNewKitButton();

        generalInformationPage.setKitName(kit.name);
        generalInformationPage.setDescription(kit.description);
        generalInformationPage.setTag(kit.tags);

        kitStockSetupPage = generalInformationPage.clickNext();

        //no needed
        //kitStockSetupPage.clickAddLocation();

        kitStockSetupPage.clickGenerateCode();
        kitStockSetupPage.clickAddCode();

        // warehouse pre-filled
        //kitStockSetupPage.setWarehouseUsingUtility(kit.location);

        kitSettingsPage = kitStockSetupPage.clickNext();
        kitSettingsPage.addFirstMaterialByName("Test");
        kitSettingsPage.setQtyForMaterialInKit(5);
        kitSettingsPage.addFirstToolByName("Test");

        //Double kitPrice = kitSettingsPage.getTheKitPrice();

        kitSettingsPage.clickBottomSave();

        AlertUtils.waitForAlertVisible(page);
        String alert = AlertUtils.getAlertText(page);
        Assertions.assertThat(alert).isEqualTo("Kit \"%s\" has been successfully created", kit.name);
        AlertUtils.waitForAlertHidden(page);

        Assertions.assertThat(kitsListPage.getFirstKitNameInTheList()).isEqualTo(kit.name);
        //Assertions.assertThat(kitsListPage.getFirstKitCostInTheListAsDouble()).isEqualTo(kitPrice);
    }

    @DisplayName("Update Kit in Location Test")
    @Order(4)
    @Test
    public void testUpdateKitInLocation() {

        openPath("/stock/warehouse/" + warehouseName.toLowerCase() + "/" + warehouseId);
        warehousePage.waitForLoaded();
        warehousePage.clickOnKitsTabButton();

        warehousePage.openFirstRowThreeDots();

        generalInformationPage = warehousePage.clickOnEditKitButton();
        generalInformationPage.setKitName(editedKit.name);
        generalInformationPage.setDescription(editedKit.description);
        generalInformationPage.setTag(editedKit.tags);

        generalInformationPage.clickSave();

        AlertUtils.waitForAlertVisible(page);
        String alert = AlertUtils.getAlertText(page);
        Assertions.assertThat(alert).isEqualTo("Kit \"%s\" has been successfully updated", editedKit.name);
        AlertUtils.waitForAlertHidden(page);

        Assertions.assertThat(kitsListPage.getFirstKitNameInTheList()).isEqualTo(editedKit.name);
    }

    @DisplayName("Delete Kit in Location Test")
    @Order(5)
    @Test
    public void testDeleteKitInLocation() {
        openPath("/stock/warehouse/" + warehouseName.toLowerCase() + "/" + warehouseId);
        warehousePage.waitForLoaded();
        warehousePage.clickOnKitsTabButton();

        String kitName = kitsListPage.getFirstKitNameInTheList();

        warehousePage.openFirstRowThreeDots();
        warehousePage.clickOnDeleteButton();
        warehousePage.confirmDeletion();

        AlertUtils.waitForAlertVisible(page);
        String alert = AlertUtils.getAlertText(page);
        Assertions.assertThat(alert).isEqualTo("Kit has been successfully deleted");
        AlertUtils.waitForAlertHidden(page);

        waitForElementRemoved(kitName);
        Assertions.assertThat(kitsListPage.getKitNamesList()).doesNotContain(kitName);
    }

}
