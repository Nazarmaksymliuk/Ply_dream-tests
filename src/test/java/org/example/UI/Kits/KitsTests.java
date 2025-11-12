package org.example.UI.Kits;

import org.assertj.core.api.Assertions;
import org.example.BaseUITestExtension.BaseUiTest;
import org.example.BaseUITestExtension.PlaywrightUiLoginBaseTest;
import org.example.UI.Models.Kit;
import org.example.UI.PageObjectModels.Alerts.AlertUtils;
import org.example.UI.PageObjectModels.Catalog.CatalogPage;
import org.example.UI.PageObjectModels.Kits.KitsCreationFlow.KitGeneralInformationPage;
import org.example.UI.PageObjectModels.Kits.KitsCreationFlow.KitSettingsPage;
import org.example.UI.PageObjectModels.Kits.KitsCreationFlow.KitStockSetupPage;
import org.example.UI.PageObjectModels.Kits.KitsListPage;
import org.junit.jupiter.api.*;

import java.util.Random;
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class KitsTests extends PlaywrightUiLoginBaseTest {
    KitGeneralInformationPage generalInformationPage;
    KitsListPage kitsListPage;
    CatalogPage catalogPage;
    KitStockSetupPage kitStockSetupPage;
    KitSettingsPage kitSettingsPage;

    @BeforeEach
    public void setUp() {
        openPath("/catalog");
        catalogPage = new CatalogPage(page);
        kitsListPage = new KitsListPage(page);
    }

    Kit kit = new Kit(
            "Kit-" + new Random().nextInt(100000),
            "High-performance kit for any type of work.",
            "test tag",
            "WarehouseMain"
    );

    @DisplayName("Create Kit Test")
    @Order(0)
    @Test
    public void testCreateKit() {
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

        //waitForElementPresent(kit.name);
        Assertions.assertThat(kitsListPage.getFirstKitNameInTheList()).isEqualTo(kit.name);
        Assertions.assertThat(kitsListPage.getFirstKitCostInTheListAsDouble()).isEqualTo(kitPrice);

    }

    Kit editedKit = new Kit(
            "Kit-edited" + new Random().nextInt(100000),
            "High-performance kit for any type of work-edited",
            "test tag-edited",
            "WarehouseMain"
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

        //waitForElementPresent(editedKit.name);
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



}
