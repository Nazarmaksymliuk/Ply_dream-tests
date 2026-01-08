package org.example.UI.PageObjectModels.FieldRequests;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.example.UI.PageObjectModels.Utils.LocationSelect;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.example.UI.PageObjectModels.Utils.Waits.WaitUtils.waitForVisible;

public class FieldRequestsPage {
    private final Page page;

    private final Locator navFieldRequestsLink;
    private final Locator createFieldRequestBtn;
    private final Locator locationSelectInputContainer;
    private final Locator locationReactSelectInput;
    private final Locator nameInput;
    private final Locator chooseDateBtn;
    private final Locator notesInput;
    private final Locator editedNotesInput;
    private final Locator continueBtn;
    private final Locator searchMaterialsBtn;
    private final Locator saveMaterialsBtn;
    private final Locator quantityPlaceholder0;
    private final Locator saveBtn;
    private final Locator closeBtn;
    private final Locator firstMaterialCheckbox;
    private final LocationSelect locationSelectByEnter;
    private final Locator firstRowFieldRequestThreeDots;
    private final Locator menuItemEdit;
    private final Locator menuItemDelete;
    private final Locator nameEditInput;
    private final Locator deleteButton;



    public FieldRequestsPage(Page page) {
        this.page = page;

        navFieldRequestsLink = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Field Requests"));
        createFieldRequestBtn = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Create Field Request")).first();

        locationSelectInputContainer = page.locator(".react_select__input-container");
        // У codegen було: #react-select-4-input — але індекс може змінюватись.
        // Тому робимо fallback: знайти input всередині react-select контейнера:
        locationReactSelectInput = page.locator(".react_select__input-container input");

        nameInput = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Enter a name"));
        nameEditInput = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Leave blank to use request's number as name"));
        chooseDateBtn = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Choose a date"));
        notesInput = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Add notes"));
        editedNotesInput = page.locator("[class^='_input_']").last();
        continueBtn = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Continue"));

        searchMaterialsBtn = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Search materials"));
        // у codegen було getByLabel('', { exact: true }).nth(1).check(); — це слабко.
        // Я зроблю метод, який чекатиме чекбокси і вибере перший доступний.
        saveMaterialsBtn = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save materials"));

        quantityPlaceholder0 = page.getByPlaceholder("0");
        saveBtn = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save"));
        closeBtn = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Close"));
        deleteButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Delete"));

        firstMaterialCheckbox = page.locator("input[type='checkbox']").nth(3);

        locationSelectByEnter = new LocationSelect(page);

        firstRowFieldRequestThreeDots = page
                .locator("[class^='_table_item_']")
                .locator("[data-testid='MoreHorizIcon']").first();

        menuItemEdit = page.getByRole(
                AriaRole.MENUITEM,
                new Page.GetByRoleOptions().setName("Edit") //// MAINNNNNNNNNN
        );
        menuItemDelete = page.getByRole(
                AriaRole.MENUITEM,
                new Page.GetByRoleOptions().setName("Delete") //// MAINNNNNNNNNN
        );
    }


    public FieldRequestsPage startCreate() {
        createFieldRequestBtn.click();
        return this;
    }

    public FieldRequestsPage selectLocation(String locationName) {
        locationSelectByEnter.setLocationByEnter(locationName);

        return this;
    }

    public FieldRequestsPage setName(String name) {
        nameInput.fill(name);
        return this;
    }
    public FieldRequestsPage setEditedName(String name) {
        nameEditInput.click();
        nameEditInput.clear();
        nameEditInput.fill(name);
        return this;
    }

    public FieldRequestsPage openEditMenuOption() {
        firstRowFieldRequestThreeDots.click();
        menuItemEdit.click();
        return this;
    }

    public FieldRequestsPage openDeleteAndConfirmMenuOption() {
        firstRowFieldRequestThreeDots.click();
        menuItemDelete.click();
        deleteButton.click();
        return this;
    }

    /**
     * Вибір дати через datepicker.
     * У JS було: option name "Choose Thursday, January 8th,"
     * Тут робимо універсальніше: будуємо aria-name як в їх datepicker.
     *
     * Якщо їх datepicker генерує інший текст — просто зміниш формат у одному місці.
     */
    public FieldRequestsPage chooseDate(LocalDate date) {
        chooseDateBtn.click();

        // Спроба відтворити текст як у codegen:
        // "Choose Thursday, January 8th,"
        String dayOfWeek = date.format(DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH));
        String monthDay = date.format(DateTimeFormatter.ofPattern("MMMM d", Locale.ENGLISH));
        String ordinal = ordinal(date.getDayOfMonth()); // 8th, 1st, 2nd...
        String optionName = String.format("Choose %s, %s%s,", dayOfWeek, monthDay, ordinal);

        Locator option = page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName(optionName));
        option.click();

        return this;
    }

    public FieldRequestsPage setNotes(String notes) {
        notesInput.fill(notes);
        return this;
    }

    public FieldRequestsPage setEditedNotes(String notes) {
        editedNotesInput.fill(notes);
        return this;
    }

    public FieldRequestsPage saveChanges() {
        saveBtn.click();
        return this;
    }

    public FieldRequestsPage continueNext() {
        continueBtn.click();
        // Очікуємо крок з матеріалами
        assertThat(searchMaterialsBtn).isVisible();
        return this;
    }

    public FieldRequestsPage addFirstMaterialFromSearch() {
        searchMaterialsBtn.click();

        waitForVisible(firstMaterialCheckbox);

        firstMaterialCheckbox.click();

        saveMaterialsBtn.click();
        return this;
    }

    public FieldRequestsPage setQuantityAndSave(String qty) {
        quantityPlaceholder0.first().click();
        quantityPlaceholder0.first().fill(qty);

        saveBtn.click();
        closeBtn.click();
        return this;
    }

    public void assertFieldRequestListed(String name) {
        assertThat(page.locator("#root")).containsText(name);
    }
    public void assertFieldRequestNotListed(String name) {
        assertThat(page.locator("#root")).not().containsText(name);
    }

    private static String ordinal(int day) {
        if (day >= 11 && day <= 13) return "th";
        return switch (day % 10) {
            case 1 -> "st";
            case 2 -> "nd";
            case 3 -> "rd";
            default -> "th";
        };
    }
}
