package org.example.UI.PageObjectModels.Stock.InventoryCount;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

// ===== Create form =====
public class CreateInventoryCountPage {
    private final Page page;

    // Вибір юзерів (react-select) — беремо останній інпут, як ти просив
    private final Locator chooseUserInput;

    // Дві кнопки дат: Start Date (nth(0)) і End Date (nth(1))
    private final Locator dateButtons;

    // Сьогоднішній день у datepicker
    private final Locator todayInDatepicker;

    // Поле Number of days (всередині контейнера з класом, що починається на _input_block_)
    private final Locator numberOfDaysInput;

    // Confirm (права верхня кнопка)
    private final Locator confirmButton;

    private final Locator warehouseNameInTheCycleCountLocator;

    public CreateInventoryCountPage(Page page) {
        this.page = page;

        this.chooseUserInput = page.locator(".react_select__input").last();

        // Обидві кнопки мають однакові класи; беремо колекцію й звертаємось по індексу
        this.dateButtons = page.locator("button._custom_input_1yyqt_1371");

        this.todayInDatepicker = page.locator(".react-datepicker__day--today").first();

        this.numberOfDaysInput = page.locator("div[class^='_input_block_'] input").last();

        this.confirmButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Confirm"));

        warehouseNameInTheCycleCountLocator = page.locator(".fw_500").first();

    }

    // --- кроки, що використовуються в тесті ---

    /** Вибрати користувача через react-select (останній інпут). */
    public void assignUserForInventoryCount(String userName) {
        chooseUserInput.waitFor();
        chooseUserInput.click();
        page.waitForTimeout(2500); // як ти і просив
        chooseUserInput.fill(userName);
        page.keyboard().press("Enter");
    }

    /** Start Date = сьогодні. */
    public void setStartDateToday() {
        dateButtons.nth(0).click();
        todayInDatepicker.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        todayInDatepicker.click();
    }

    /** End Date = сьогодні. */
    public void setEndDateToday() {
        dateButtons.nth(1).click();
        todayInDatepicker.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        todayInDatepicker.click();
    }

    /** Встановити Number of days. */
    public void setNumberOfDays(int days) {
        numberOfDaysInput.fill(String.valueOf(days));
    }

    /** Підтвердити створення інвентаризації. */
    public void clickConfirm() {
        confirmButton.click();
    }

    public Locator getTheWarehouseMainLocator(){
        return warehouseNameInTheCycleCountLocator;
    }


}
