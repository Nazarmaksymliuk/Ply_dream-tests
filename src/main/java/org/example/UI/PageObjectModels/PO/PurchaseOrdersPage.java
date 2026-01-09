package org.example.UI.PageObjectModels.PO;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.example.UI.PageObjectModels.Utils.Waits.WaitUtils.waitForVisible;

public class PurchaseOrdersPage {

    private final Page page;

    // Nav / main actions
    private final Locator purchaseOrdersNavLink;
    private final Locator createNewBtn;
    private final Locator newPOBtn;

    // Form fields
    private final Locator numberInput;
    private final Locator chooseDateBtn;

    // react-select
    private final Locator reactSelectInputs;

    // Items flow
    private final Locator addItemBtn;
    private final Locator addItemsBtn;
    private final Locator qtyInput;

    // Submit
    private final Locator createDraftBtn;

    // ===== EDIT FLOW (NEW) =====
    private final Locator firstRowActionsBtn;     // "..." (3 dots) in first row
    private final Locator editButtonInMenu;       // Edit пункт в меню
    private final Locator updatePurchaseOrderBtn; // Update Purchase Order
    private final Locator alertToast;             // role=alert

    private final Locator shipToAsShippingAddressRadioBtn;

    private final Locator deleteButtonInMenu;

    private final Locator deletePurchaseOrderBtn;


    public PurchaseOrdersPage(Page page) {
        this.page = page;

        purchaseOrdersNavLink = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Purchase Orders"));
        createNewBtn = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Create New"));
        newPOBtn = page.getByRole(AriaRole.MENUITEM, new Page.GetByRoleOptions().setName("New PO"));

        numberInput = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Enter number"));
        chooseDateBtn = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Choose a date"));

        reactSelectInputs = page.locator(".react_select__input-container input");

        addItemBtn = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Item"));
        addItemsBtn = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Items"));

        qtyInput = page.getByPlaceholder("Qty");

        createDraftBtn = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Create Draft Purchase Order"));

        // ===== EDIT FLOW (NEW) =====
        // З твого codegen:
        // page.locator(".MuiButtonBase-root.MuiIconButton-root.MuiIconButton-sizeSmall.\\!w-\\[28px\\]").first().click();
        firstRowActionsBtn = page.locator(".MuiButtonBase-root.MuiIconButton-root.MuiIconButton-sizeSmall.\\!w-\\[28px\\]").first();

        // У codegen воно виглядало як BUTTON з ім'ям "Edit Copy PO Create Send to"
        // Тому беремо кнопку, яка містить текст "Edit"
        editButtonInMenu = page.getByRole(AriaRole.MENUITEM)
                .filter(new Locator.FilterOptions().setHasText("Edit"))
                .first();

        deleteButtonInMenu = page.getByRole(AriaRole.MENUITEM)
                .filter(new Locator.FilterOptions().setHasText("Delete"))
                .first();

        deletePurchaseOrderBtn = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Delete Purchase Order")
        );

        updatePurchaseOrderBtn = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Update Purchase Order"));
        alertToast = page.getByRole(AriaRole.ALERT);

        shipToAsShippingAddressRadioBtn = page.locator("[class*='MuiSwitch-root']").first();
    }

    public PurchaseOrdersPage startCreateNew() {
        if (purchaseOrdersNavLink.isVisible()) {
            purchaseOrdersNavLink.click();
        }
        createNewBtn.click();
        return this;
    }

    public PurchaseOrdersPage chooseNewPO() {
        newPOBtn.click();
        assertThat(numberInput).isVisible();
        return this;
    }

    public PurchaseOrdersPage setNumber(String number) {
        waitForVisible(numberInput);
        numberInput.click();
        numberInput.fill(number);
        return this;
    }

    public PurchaseOrdersPage selectFirstReactSelectByTyping(String value) {
        Locator first = reactSelectInputs.nth(0);
        waitForVisible(first);
        first.click();
        first.fill(value);
        page.waitForTimeout(2500);
        first.press("Enter");
        return this;
    }

    public PurchaseOrdersPage setShipToAsShippingAddress() {
        shipToAsShippingAddressRadioBtn.click();
        return this;
    }



    public PurchaseOrdersPage selectSecondReactSelectByTyping(String value) {
        Locator second = reactSelectInputs.last();
        waitForVisible(second);
        second.click();
        second.fill(value);
        page.waitForTimeout(2500);
        second.press("Enter");
        return this;
    }

    public PurchaseOrdersPage chooseNeedByDate(LocalDate date) {
        chooseDateBtn.click();

        String dayOfWeek = date.format(DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH));
        String monthDay = date.format(DateTimeFormatter.ofPattern("MMMM d", Locale.ENGLISH));
        String suffix = ordinal(date.getDayOfMonth());
        String optionName = String.format("Choose %s, %s%s,", dayOfWeek, monthDay, suffix);

        Locator option = page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName(optionName));
        waitForVisible(option);
        option.click();

        return this;
    }

    public PurchaseOrdersPage addItem() {
        addItemBtn.click();
        return this;
    }

    public PurchaseOrdersPage selectFirstMaterialRowAndAdd() {
        Locator firstRowCheckbox = page.getByLabel("Select row").first();
        waitForVisible(firstRowCheckbox);
        firstRowCheckbox.check();

        addItemsBtn.click();
        return this;
    }

    public PurchaseOrdersPage setQty(String qty) {
        waitForVisible(qtyInput);
        qtyInput.click();
        qtyInput.clear();
        qtyInput.fill(qty);
        return this;
    }

    public PurchaseOrdersPage createDraftPurchaseOrder() {
        createDraftBtn.click();
        return this;
    }

    // ===== EDIT METHODS (NEW) =====

    public PurchaseOrdersPage openFirstRowActions() {
        waitForVisible(firstRowActionsBtn);
        firstRowActionsBtn.click();
        return this;
    }

    public PurchaseOrdersPage clickEditFromMenu() {
        waitForVisible(editButtonInMenu);
        editButtonInMenu.click();

        // на всяк: переконатись що відкрився edit form
        assertThat(numberInput).isVisible();
        return this;
    }

    public PurchaseOrdersPage clickDeleteFromMenu() {
        waitForVisible(deleteButtonInMenu);
        deleteButtonInMenu.click();
        return this;
    }

    public PurchaseOrdersPage deletePurchaseOrder() {
        waitForVisible(deletePurchaseOrderBtn);
        deletePurchaseOrderBtn.click();
        return this;
    }

    public PurchaseOrdersPage updatePurchaseOrder() {
        waitForVisible(updatePurchaseOrderBtn);
        updatePurchaseOrderBtn.click();
        return this;
    }

    public PurchaseOrdersPage assertUpdateSuccess() {
        assertThat(alertToast).isVisible();
        return this;
    }

    public void assertPurchaseOrderListed(String poNumber) {
        assertThat(page.locator("#root")).containsText(poNumber);
    }

    public void assertPurchaseOrderNotListed(String poNumber) {
        assertThat(page.locator("#root")).not().containsText(poNumber);
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
