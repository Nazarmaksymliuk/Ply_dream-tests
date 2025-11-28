package org.example.UI.PageObjectModels.Material;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.example.UI.PageObjectModels.Material.MaterialsCreationFlow.MaterialSpecsPage;
import org.example.UI.PageObjectModels.Transfer.TransferModalPage;

import java.util.List;


import static com.microsoft.playwright.Page.*;


public class MaterialsListPage {
    private final Page page;

    private final Locator materialFirstNameInTheList;
    private final Locator materialNamesInTheList;
    private final Locator firstItemNumberInTheList;
    private final Locator firstMaterialVariation;

    private final Locator firstRowThreeDots;
    private final Locator menuItemEdit;
    private final Locator menuItemDelete;
    private final Locator deleteMaterialInConfirmationModalButton;

    private final Locator openLocationsListButton;
    private final Locator firstLocationArrowDownButton;
    private final Locator materialLocationInTheDropDown;
    private final Locator qtyInMaterialLocation;
    private final Locator firstRowMaterialStockThreeDots;

    private final Locator menuItemEditAvailability;

    private final Locator qtySpan;

    private final Locator searchByItemInput;

    // === ДОПОВНЕНО ДЛЯ ТРАНСФЕРУ ===
    private final Locator firstRowQtyButton;      // кнопка “X Each” у першому рядку
    private final Locator firstRowQty;      // кнопка “X Each” у першому рядку
    private final Locator firstRowCheckbox;       // чекбокс першого рядка
    private final Locator transferToolbarButton;  // глобальна кнопка Transfer


    public MaterialsListPage(Page page) {
        this.page = page;

        materialFirstNameInTheList = page.locator("a.link_black[href^='/material/']").first();
        materialNamesInTheList      = page.locator("a.link_black[href^='/material/']");
        firstItemNumberInTheList    = page.locator(".cursor-pointer.w-fit").first();
        firstMaterialVariation      = page.locator(".status_xs.variation_name").first();

        firstRowThreeDots = page.locator("[class^='_table_item_'][data-testid='MoreHorizIcon']");
        menuItemEdit      = page.getByRole(AriaRole.MENUITEM,   new GetByRoleOptions().setName("Edit Material"));
        menuItemEditAvailability      = page.getByRole(AriaRole.MENUITEM,   new GetByRoleOptions().setName("Edit availability"));
        menuItemDelete    = page.getByRole(AriaRole.MENUITEM,   new GetByRoleOptions().setName("Delete"));
        deleteMaterialInConfirmationModalButton =
                page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Delete"));

        openLocationsListButton     = page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Locations"));
        firstLocationArrowDownButton= page.getByTestId("KeyboardArrowDownIcon").first();
        materialLocationInTheDropDown = page.locator("[href^='/stock/warehouse']");
        qtyInMaterialLocation       = page.locator("b.flex.items-center.gap-1");

        firstRowMaterialStockThreeDots = page.locator("[class^='_table_item_'] [data-testid='MoreHorizIcon']").first();

        qtySpan = page.locator("div[role='button'][class^='_edit_wrapper_'] b span:nth-of-type(1)").first();


        searchByItemInput = page.getByPlaceholder("Search by item")
                .or(page.getByPlaceholder("Search material")).or(page.getByPlaceholder("Search..."));

        // === нові локатори для трансферу ===
        // кнопка кількості у першому рядку — шукаємо кнопку, що містить "Each"
        firstRowQtyButton = page.locator("div[role='button']:has(span[aria-label='Each'])").first();

        firstRowQty = page.locator(".inline-flex").first();

        firstRowCheckbox      = page.getByRole(AriaRole.CHECKBOX).nth(1);
        transferToolbarButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Transfer"));

    }

    // ===== Waits =====
    public void waitFirstRowVisible() {
        materialFirstNameInTheList.waitFor(
                new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(60000)
        );
    }

//    // ===== допоміжне: кількість у першому рядку з гріда
//    public int getFirstRowQuantity() {
//        Locator qtySpan = page.locator("div[role='button'][class^='_edit_wrapper_'] b span:nth-of-type(1)").first();
//        String text = qtySpan.innerText().trim().replaceAll("[^\\d]", "");
//        if (text.isEmpty()) return 0;
//        return Integer.parseInt(text);
//    }

    // ===== редагування кількості першого рядка у гріді
    public void setFirstRowGridQuantity(int value) {
        // відкриваємо поповер/інлайн-редактор кількості
        firstRowQtyButton.click();

        // у деяких версіях інпут — це textbox всередині “кнопки з числом”
        // спробуємо знайти перший видимий текстовий інпут у відкритому поповері
        Locator qtyEditorInput = page.locator("div[class^='_table_item_'] [class^='_input_']").first();
        qtyEditorInput.clear();
        qtyEditorInput.fill(Integer.toString(value));

        // підтвердження — Enter (у codegen був клік по якомусь ._button_*)
        page.keyboard().press("Enter");

        page.waitForTimeout(1500);

    }

    public int getQtyFromMaterialLocationStock() {
        String text = firstRowQty.innerText().replaceAll("[^\\d.]+", ""); // залишає лише цифри та крапку
        if (text.isEmpty()) return 0;
        return Integer.parseInt(text);
    }

    // ===== чекбокс + Transfer
    public void checkFirstRow() {
        firstRowCheckbox.check();
    }

    public TransferModalPage clickTransferButton() {
        transferToolbarButton.click();
        return new TransferModalPage(page);
    }

    // вузький якір: рядок матеріалу за назвою
    private Locator materialRowByName(String name) {
        return page.locator("a.link_black[href^='/material/']")
                .filter(new Locator.FilterOptions().setHasText(name))
                .first();
    }

    // опційний спінер (fallback: будь-який прогресбар)
    private Locator searchSpinner() {
        return page.getByRole(com.microsoft.playwright.options.AriaRole.PROGRESSBAR);
    }

    // «введи терм і дочекайся, що з’явився конкретний матеріал»
    public void searchByItem(String expectedName) {
        // 1) фокус та чистка поля
        searchByItemInput.click();
        searchByItemInput.fill("");
        page.keyboard().press("Control+A");
        page.keyboard().press("Backspace");

        page.waitForTimeout(2500);
        // 3) якщо є спінер — дочекаємося, що він зник (не валимося, якщо його немає)

        // 2) вводимо терм
        searchByItemInput.fill(expectedName);

        page.waitForTimeout(2500);

        waitFirstRowVisible();
        // якщо пошук підтверджується Enter — розкоментуй:
        // page.keyboard().press("Enter");


    }

    public void clearSearch() {
        searchByItemInput.fill("");
        waitFirstRowVisible();
    }

    public boolean isMaterialWithNamePresent(String name) {
        return materialFirstNameInTheList.filter(new Locator.FilterOptions().setHasText(name)).count() > 0;
    }

    // ===== Row menus =====
    public void openFirstRowThreeDots() {
        firstRowThreeDots.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        firstRowThreeDots.click();
    }

    public MaterialSpecsPage chooseMenuEditMaterial() {
        menuItemEdit.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        menuItemEdit.click();
        return new MaterialSpecsPage(page);
    }
    public MaterialSpecsPage chooseMenuEditMaterialAvailability() {
        menuItemEditAvailability.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        menuItemEditAvailability.click();
        return new MaterialSpecsPage(page);
    }

    public void chooseMenuDeleteMaterial() {
        menuItemDelete.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        menuItemDelete.click();
    }

    public void confirmDeleteMaterialInModal() {
        deleteMaterialInConfirmationModalButton.click();
    }

    // ===== Locations dropdown / qty =====
    public void openLocationsList() {
        openLocationsListButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        openLocationsListButton.click();
    }

    public void clickFirstLocationArrowDown() {
        firstLocationArrowDownButton.click();
    }

    public String getMaterialLocationFromDropdown() {
        return materialLocationInTheDropDown.innerText();
    }

    public double getQtyFromMaterialLocation() {
        String text = qtyInMaterialLocation.innerText().replaceAll("[^\\d.]+", ""); // залишає цифри й крапку
        if (text.isEmpty()) return 0.0;
        return Double.parseDouble(text);
    }
    public void openFirstRowMaterialStockThreeDots() {
        firstRowMaterialStockThreeDots.waitFor(
                new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE)
        );
        firstRowMaterialStockThreeDots.click();
    }

    // ===== Getters from list =====
    public String getFirstMaterialNameInTheList() {
        return materialFirstNameInTheList.innerText();
    }

    public String getFirstItemNumberInTheList() {
        return firstItemNumberInTheList.innerText();
    }

    public String getFirstMaterialVariation() {
        return firstMaterialVariation.innerText();
    }

    public List<String> getMaterialNamesList() {
        return materialNamesInTheList.allInnerTexts();
    }

    /** Кількість з першого рядка гріда (значення у <b><span>10</span>...) */
    public int getFirstRowQuantity() {
        Locator qtySpan = page.locator("div[role='button'][class^='_edit_wrapper_'] b span:nth-of-type(1)").first();
        String text = qtySpan.innerText().trim();

        // приберемо все, крім цифр
        text = text.replaceAll("[^\\d]", "");

        // якщо раптом пусто — повернемо 0, щоб уникнути NumberFormatException
        if (text.isEmpty()) return 0;

        return Integer.parseInt(text);
    }


}
