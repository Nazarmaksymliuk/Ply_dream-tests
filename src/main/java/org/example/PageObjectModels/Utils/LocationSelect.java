package org.example.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * Компонент для вибору локації (react-select) зі стандартними селекторами:
 *  - control:  div.react_select__control
 *  - input:    div.react_select__input-container input
 *  - options:  div.react_select__menu-list div.react_select__option
 *
 * Можна використовувати як для warehouse/location, так і для подібних дропдаунів.
 */
public class LocationSelect {
    private final Page page;
    private final Locator control;       // клік для відкриття списку
    private final Locator input;         // поле введення для фільтрації
    private final Locator options;       // список опцій


    private final Locator locationSelect;

    /**
     * @param page Playwright Page
     *                      якщо хочеш шукати по всій сторінці — передай page.locator("body")
     */
    public LocationSelect(Page page) {
        this.page = page;

        this.control = page.locator("div.react_select__control");
        this.input   = page.locator("div.react_select__input-container input");
        this.options = page.locator("div.react_select__menu-list div.react_select__option");

        locationSelect      = page.locator("input[class='react_select__input']").first();

    }

    public void setLocationByEnter(String location) {
        locationSelect.type(location);
        page.waitForTimeout(1000);
        page.keyboard().press("Enter");
    }


    /** Відкрити селект */
    public void open() {
        control.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(15000));
        control.click();
    }

    /** Надрукувати фільтр у input (react-select) */
    public void type(String text) {
        input.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(10000));
        input.fill(text);
    }

    /** Обрати першу опцію, що містить текст */
    public void chooseContains(String text) {
        Locator candidate = options.filter(new Locator.FilterOptions().setHasText(text)).first();
        candidate.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(10000));
        candidate.click();
    }

    /** Обрати опцію з точним збігом тексту (коли важлива точність) */
    public void chooseExact(String exactText) {
        Locator candidate = options.filter(new Locator.FilterOptions().setHasText(exactText))
                .filter(new Locator.FilterOptions().setHasNotText(exactText + " ")) // грубий фільтр від "починається з"
                .first();
        candidate.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(10000));
        candidate.click();
    }

    /** Повна дія: відкрити, надрукувати, обрати перший збіг (contains) */
    public void select(String locationName) {
        open();
        type(locationName);
        chooseContains(locationName);
    }

    /** Те саме, але з точним збігом */
    public void selectExact(String locationName) {
        open();
        type(locationName);
        chooseExact(locationName);
    }



}
