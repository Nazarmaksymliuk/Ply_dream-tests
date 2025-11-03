package org.example.UI.Stock.Templates;

import net.datafaker.Faker;
import org.assertj.core.api.Assertions;
import org.example.BaseUITestExtension.PlaywrightUiLoginBaseTest;
import org.example.UI.PageObjectModels.Alerts.AlertUtils;
import org.example.UI.PageObjectModels.Stock.StockPage;
import org.example.UI.PageObjectModels.Stock.Templates.CreateLocationTemplatePage;
import org.example.UI.PageObjectModels.Stock.Templates.TemplatesListPage;
import org.junit.jupiter.api.*;

import java.util.Random;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LocationTemplateTest extends PlaywrightUiLoginBaseTest {
    StockPage stockPage;
    TemplatesListPage templatesListPage;
    CreateLocationTemplatePage createLocationTemplatePage;

    Faker faker = new Faker();
    String templateName = "AQA-Template-" + new Random().nextInt(100_000);
    String templateNameEdited = "AQA-Template-Edited-" + new Random().nextInt(100_000);

    @BeforeEach
    public void setUp() {
        openPath("/stock");
        stockPage = new StockPage(page);

        templatesListPage = new TemplatesListPage(page);
    }

    @DisplayName("Create Location Template with one material (qty=10, min=1, max=5)")
    @Order(0)
    @Test
    public void createLocationTemplateTest() {
        stockPage.waitForLoaded();

        stockPage.clickOnTemplatesTab();
        templatesListPage.waitForLoaded();

        // Start create flow
        createLocationTemplatePage = templatesListPage.clickAddTemplate();
        createLocationTemplatePage.waitForLoaded();

        // Fill template name
        createLocationTemplatePage.setTemplateName(templateName);

        // Search material by generic term and pick the first
        createLocationTemplatePage.searchMaterial("Test");
        createLocationTemplatePage.addFirstMaterialFromSearch();

        // Fill fields for the first (and only) added material
        createLocationTemplatePage.setFirstMaterialQuantity(10);
        createLocationTemplatePage.setFirstMaterialMinAmount(1);
        createLocationTemplatePage.setFirstMaterialMaxAmount(5);

        // Save
        createLocationTemplatePage.clickCreate();

        AlertUtils.waitForAlertVisible(page);
        Assertions.assertThat(AlertUtils.getAlertText(page)).isEqualTo("Template was created successfully");
        AlertUtils.waitForAlertHidden(page);

        templatesListPage.waitForLoaded();
        // Assert: name is present on the grid
        Assertions.assertThat(templatesListPage.getTemplateNamesList()).contains(templateName);
    }
    @DisplayName("Update Location Template name")
    @Order(1)
    @Test
    public void updateTemplateNameTest() {
        stockPage.waitForLoaded();

        stockPage.clickOnTemplatesTab();
        templatesListPage.waitForLoaded();

        // 1) Поточна назва першого темплейта
        String oldName = templatesListPage.getFirstTemplateName();

        // 2) Три крапки → Edit template name
        templatesListPage.clickOnThreeDotsButton();
        templatesListPage.menuItemEditButton();

        // 3) Попап редагування (використовуємо CreateLocationTemplatePage)
        CreateLocationTemplatePage editPopup = new CreateLocationTemplatePage(page);
        editPopup.setTemplateName(templateNameEdited);
        editPopup.clickSaveChanges();

        AlertUtils.waitForAlertVisible(page);
        String alert = AlertUtils.getAlertText(page);
        // текст може відрізнятися — лишимо contains
        Assertions.assertThat(alert).contains("successfully");
        AlertUtils.waitForAlertHidden(page);

        // 5) Перевірити, що у списку з’явилась нова назва
        templatesListPage.waitForLoaded();
        Assertions.assertThat(templatesListPage.getTemplateNamesList()).contains(templateNameEdited);
        // (опційно) і що стара зникла:
        Assertions.assertThat(templatesListPage.getTemplateNamesList()).doesNotContain(oldName);
    }

    @DisplayName("Delete Location Template (via 3 dots → Delete)")
    @Order(0)
    @Test
    public void deleteTemplateTest() {

        stockPage.waitForLoaded();

        stockPage.clickOnTemplatesTab();
        templatesListPage.waitForLoaded();

        // 1) Візьмемо назву першого темплейта
        String templateNameToDelete = templatesListPage.getFirstTemplateName();

        // 2) “…” → Delete
        templatesListPage.clickOnThreeDotsButton();
        templatesListPage.menuItemDeleteButton();

        // 3) Підтвердження у попапі: кнопка з назвою темплейта
        templatesListPage.confirmRemoveTemplateModal();

        // 4) (опційно) тост/алерт про успіх
        try {
            AlertUtils.waitForAlertVisible(page);
            String alert = AlertUtils.getAlertText(page);
            Assertions.assertThat(alert.toLowerCase()).contains("deleted");
            AlertUtils.waitForAlertHidden(page);
        } catch (Exception ignored) {}

        // 5) Перевірка, що темплейт зник зі списку
        templatesListPage.waitForLoaded();
        Assertions.assertThat(templatesListPage.getTemplateNamesList())
                .doesNotContain(templateNameToDelete);

    }

}
