package org.example.UI.PageObjectModels.Stock.Templates;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

public class CreateLocationTemplatePage {
    private final Page page;

    // Поля
    private final Locator templateNameInput;   // "Enter template name"
    private final Locator searchMaterialsInput; // "Search materials"

    // Дії з матеріалом (беремо перший у результатах)
    private final Locator addFirstMaterialButton; // "Add" (перший)
    private final Locator firstQuantityInput;     // "Enter quantity" (first)
    private final Locator firstMinAmountInput;    // "Enter min amount" (first)
    private final Locator firstMaxAmountInput;    // "Enter max amount" (first)

    private final Locator createButton;           // "Create"
    private final Locator dialogRootOrViewRoot;   // якір для очікування

    private final Locator saveChangesButton;


    public CreateLocationTemplatePage(Page page) {
        this.page = page;

        dialogRootOrViewRoot = page.locator("#root"); // або getByRole(DIALOG), якщо це модалка

        templateNameInput    = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Enter template name"));
        searchMaterialsInput = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Search materials"));

        addFirstMaterialButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add")).first();

        firstQuantityInput  = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Enter quantity")).first();
        firstMinAmountInput = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Enter min amount")).first();
        firstMaxAmountInput = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Enter max amount")).first();

        createButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Create"));

        saveChangesButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save Changes"));

    }

    public void waitForLoaded() {
        dialogRootOrViewRoot.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE).setTimeout(20_000));
        templateNameInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public void setTemplateName(String name) {
        templateNameInput.fill(name);
    }

    public void searchMaterial(String term) {
        searchMaterialsInput.fill(term);
        searchMaterialsInput.click();
        // зазвичай краще дочекатись результатів; якщо є лоадер — додай відповідне очікування
    }

    public void addFirstMaterialFromSearch() {
        addFirstMaterialButton.click();
    }

    public void setFirstMaterialQuantity(int qty) {
        firstQuantityInput.fill(Integer.toString(qty));
    }

    public void setFirstMaterialMinAmount(int min) {
        firstMinAmountInput.fill(Integer.toString(min));
    }

    public void setFirstMaterialMaxAmount(int max) {
        firstMaxAmountInput.fill(Integer.toString(max));
    }

    public void clickCreate() {
        createButton.click();
    }

    public void clickSaveChanges() { saveChangesButton.click(); }

}
