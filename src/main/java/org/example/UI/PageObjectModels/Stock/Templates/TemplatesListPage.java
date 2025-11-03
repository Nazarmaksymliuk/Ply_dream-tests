package org.example.UI.PageObjectModels.Stock.Templates;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.util.List;

public class TemplatesListPage {
    private final Page page;

    private final Locator addTemplateButton;
    // Кнопки виду: "<name> Materials: X"
    private final Locator templateButtons;

    private final Locator templateNamesList;

    private final Locator firstTempalateDots;

    private final Locator menuItemEdit;

    private final Locator menuItemDelete;

    private final Locator confirmRemoveTemplateButton;




    public TemplatesListPage(Page page) {
        this.page = page;
        this.addTemplateButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Template"));
        this.templateButtons = page.getByRole(AriaRole.BUTTON)
                .filter(new Locator.FilterOptions().setHasText("Materials:"));

        this.templateNamesList = page.locator(".text-base");

        firstTempalateDots = page.locator("[data-testid='MoreHorizIcon']").first();

        menuItemEdit = page.getByRole(AriaRole.MENUITEM, new Page.GetByRoleOptions().setName("Edit template name"));

        this.menuItemDelete = page.getByRole(AriaRole.MENUITEM, new Page.GetByRoleOptions().setName("Delete"));

        this.confirmRemoveTemplateButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Remove"));


    }

    public void waitForLoaded() {
        addTemplateButton.waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                .setTimeout(20_000));
    }

    public CreateLocationTemplatePage clickAddTemplate() {
        addTemplateButton.click();
        return new CreateLocationTemplatePage(page);
    }

    public void clickOnThreeDotsButton(){
        firstTempalateDots.click();
    }

    public void menuItemEditButton(){
        menuItemEdit.click();
    }

    public void menuItemDeleteButton() {
        menuItemDelete.click();
    }

    public void confirmRemoveTemplateModal() {
        confirmRemoveTemplateButton.click();
    }


    /** Повертає назву першого темплейта у списку (обрізаємо " Materials: X"). */
    public String getFirstTemplateName() {
        return templateNamesList.first().innerText();
    }

    public List<String> getTemplateNamesList() {
        return templateNamesList.allInnerTexts();
    }


}
