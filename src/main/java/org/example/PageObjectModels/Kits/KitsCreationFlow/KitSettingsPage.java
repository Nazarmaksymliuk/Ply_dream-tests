package org.example.PageObjectModels.Kits.KitsCreationFlow;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

public class KitSettingsPage {
    private final Page page;

    // Tabs
    private final Locator materialsTab;
    private final Locator toolsTab;

    // Search inputs
    private final Locator materialsSearchInput;
    private final Locator toolsSearchInput;

    // Save buttons
    // На екрані зазвичай є 2 "Save": під "Kit Price" і нижній у футері.
    // Перший (first()) відповідає блоку "Kit Price", другий (last()) — футеру.
    private final Locator saveButtons;

    // Універсальний локатор для "+ Add" (в межах відкритого списку-поповера)
    private final Locator addButtonsInPopover;

    private final Locator kitPriceDiv;

    public KitSettingsPage(Page page) {
        this.page = page;

        // Tabs
        materialsTab = page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("Materials"));
        toolsTab     = page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("Tools"));

        // Search inputs (за плейсхолдерами зі скрінів)
        materialsSearchInput = page.getByPlaceholder("Search materials");
        toolsSearchInput     = page.getByPlaceholder("Search tools");

        // Save (обидві кнопки)
        saveButtons = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save"));

        // Кнопки "+ Add" у списку вибору
        addButtonsInPopover = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add"));

        kitPriceDiv = page.locator("[class='fs_13 fw_600']").nth(2);

    }

    // ======== Materials flow ========
    public void addFirstMaterialByName(String materialName) {
        // перейти на вкладку (про всяк випадок)
        materialsTab.click();

        materialsSearchInput.click();
        materialsSearchInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        materialsSearchInput.fill(materialName);
        //materialsSearchInput.click();

        // Очікуємо появу першої кнопки "+ Add" у дропдауні/модалі
        addButtonsInPopover.first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        addButtonsInPopover.first().click();

        // Save під "Kit Price" (перший "Save" у DOM)
        //clickSectionSave();
    }

    // ======== Tools flow ========
    public void addFirstToolByName(String toolName) {
        // Перейти на вкладку Tools через role='tab'
        toolsTab.click();

        toolsSearchInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        toolsSearchInput.click();
        toolsSearchInput.fill(toolName);

        addButtonsInPopover.first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        addButtonsInPopover.first().click();


        // Save під "Kit Price" (перший "Save")
        clickSectionSave();
    }

    // ======== Save helpers ========
    /** Натискає верхній Save (у блоці "Kit Price"). */
    public void clickSectionSave() {
        saveButtons.first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        saveButtons.first().click();
    }

    public Double getTheKitPrice() {

        kitPriceDiv.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        String priceText = kitPriceDiv.innerText();

        priceText = priceText.replaceAll("[^0-9.,]", ""); // залишає лише цифри і роздільник
        priceText = priceText.replace(",", "."); // якщо десь є кома замість крапки

        return Double.parseDouble(priceText);
    }


    /** Натискає нижній Save (фінальне збереження всього кита). */
    public void clickBottomSave() {
        saveButtons.last().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        saveButtons.last().click();
    }

    // ======== Комбінований сценарій з умови ========
    /**
     * Повна дія з умови:
     * 1) у Materials ввести назву, додати перший елемент, натиснути Save;
     * 2) перейти у Tools, ввести назву, додати перший елемент, натиснути Save.
     */
    public void addMaterialAndToolAndSave(String materialName, String toolName) {
        addFirstMaterialByName(materialName);
        addFirstToolByName(toolName);
    }
}
