package org.example.UI.PageObjectModels.PO;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.example.config.TestEnvironment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.example.UI.PageObjectModels.Utils.Waits.WaitUtils.waitForLoaderDetached;
import static org.example.UI.PageObjectModels.Utils.Waits.WaitUtils.waitForVisible;

public class PurchaseOrdersPage {

    private final Page page;

    // Nav / main actions
    private final Locator purchaseOrdersNavLink;
    private final Locator createNewBtn;
    private final Locator newPOBtn;

    // Form fields — Order Details
    private final Locator numberInput;
    private final Locator numberInputInEditMode;
    private final Locator poTypeSelectInput;
    private final Locator deliveryRadioBtn;
    private final Locator pickUpRadioBtn;

    // Form fields — Shipping Information
    private final Locator chooseDateBtn;
    private final Locator shipToAsShippingAddressToggle;

    // Form fields — react-selects (using custom react_select__ class)
    // Matched by .react_select__input-container input:
    //   nth(0) = Choose Location (Ship To)
    //   nth(1) = Search job
    //   nth(2) = Contact Name
    //   nth(3) = Assigned users
    //   last() = Search and select supplier
    private final Locator reactSelectInputs;

    // Form fields — Additional Information
    private final Locator commentTextarea;

    // Items flow
    private final Locator addItemBtn;
    private final Locator addItemsBtn;
    private final Locator qtyInput;

    // Submit buttons
    private final Locator createDraftBtn;
    private final Locator updatePurchaseOrderBtn;

    // Row actions
    private final Locator firstRowActionsBtn;
    private final Locator editButtonInMenu;
    private final Locator deleteButtonInMenu;
    private final Locator deletePurchaseOrderBtn;

    private final Locator alertToast;

    // ─── Validation error locators (negative tests) ───────────────────────────
    // Ship To: wrapper gets class "react_select_wrapper_error" on validation fail;
    // filtered by text "Ship To" to avoid matching Supplier's wrapper.
    private final Locator shipToValidationError;
    // Required By: date-picker wrapper has "_date_picker_wrapper_" in class name.
    private final Locator requiredByValidationError;
    // Supplier: same wrapper error class, filtered by text "Supplier".
    private final Locator supplierValidationError;

    public PurchaseOrdersPage(Page page) {
        this.page = page;

        // Nav
        purchaseOrdersNavLink = page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName("Purchase Orders"));
        createNewBtn = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Create New"));
        newPOBtn = page.getByRole(AriaRole.MENUITEM,
                new Page.GetByRoleOptions().setName("New PO"));

        // PO Number — placeholder "Auto-generated", no aria-label present in DOM
        numberInput = page.getByPlaceholder("Auto-generated");
        numberInputInEditMode = page.getByPlaceholder("Enter number");

        poTypeSelectInput = page.locator("div")
                .filter(new Locator.FilterOptions().setHasText("PO type"))
                .locator("input")
                .first();

        deliveryRadioBtn = page.getByLabel("Delivery");
        pickUpRadioBtn   = page.getByLabel("Pick up");

        chooseDateBtn = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Choose a date"));

        shipToAsShippingAddressToggle = page.locator("[class*='MuiSwitch-root']").first();

        reactSelectInputs = page.locator(".react_select__input-container input");

        commentTextarea = page.getByPlaceholder("Type your message");

        // Items
        addItemBtn  = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Add Item"));
        addItemsBtn = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Add Items"));
        qtyInput    = page.getByPlaceholder("Qty");

        // Submit
        createDraftBtn = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Create Draft Purchase Order"));
        updatePurchaseOrderBtn = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Update Purchase Order"));

        firstRowActionsBtn = page.getByTestId("ply_menu_control_button").nth(2);

        editButtonInMenu = page.getByRole(AriaRole.MENUITEM)
                .filter(new Locator.FilterOptions().setHasText("Edit"))
                .first();
        deleteButtonInMenu = page.getByRole(AriaRole.MENUITEM)
                .filter(new Locator.FilterOptions().setHasText("Delete"))
                .first();
        deletePurchaseOrderBtn = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Delete Purchase Order"));

        alertToast = page.getByRole(AriaRole.ALERT);

        // ─── Validation errors ────────────────────────────────────────────────
        // filter() ties each error span to its field label text,
        // so these stay correct even when only one error is visible at a time.
        shipToValidationError = page.locator(".react_select_wrapper_error")
                .filter(new Locator.FilterOptions().setHasText("Ship To"))
                .locator("span.input_error");

        requiredByValidationError = page.locator("[class*='_date_picker_wrapper_'] span.input_error");

        supplierValidationError = page.locator(".react_select_wrapper_error")
                .filter(new Locator.FilterOptions().setHasText("Supplier"))
                .locator("span.input_error");
    }

    // ─── Navigation / open form ───────────────────────────────────────────────

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

    // ─── Order Details fields ─────────────────────────────────────────────────

    public PurchaseOrdersPage setNumber(String number) {
        waitForVisible(numberInput);
        numberInput.click();
        numberInput.fill(number);
        return this;
    }

    public PurchaseOrdersPage setNumberInEditMode(String number) {
        waitForVisible(numberInputInEditMode);
        numberInputInEditMode.click();
        numberInputInEditMode.fill(number);
        return this;
    }

    public PurchaseOrdersPage setPoType(String value) {
        waitForVisible(poTypeSelectInput);
        poTypeSelectInput.click();
        poTypeSelectInput.fill(value);
        page.waitForTimeout(TestEnvironment.SEARCH_DELAY_MS);
        poTypeSelectInput.press("Enter");
        return this;
    }

    public PurchaseOrdersPage selectDelivery() {
        deliveryRadioBtn.click();
        return this;
    }

    public PurchaseOrdersPage selectPickUp() {
        pickUpRadioBtn.click();
        return this;
    }

    // ─── Shipping Information ─────────────────────────────────────────────────

    public PurchaseOrdersPage selectFirstReactSelectByTyping(String value) {
        Locator first = reactSelectInputs.nth(0);
        waitForVisible(first);
        first.click();
        first.fill(value);
        page.waitForTimeout(TestEnvironment.SEARCH_DELAY_MS);
        first.press("Enter");
        return this;
    }

    public PurchaseOrdersPage setShipToAsShippingAddress() {
        shipToAsShippingAddressToggle.click();
        return this;
    }

    public PurchaseOrdersPage chooseNeedByDate(LocalDate date) {
        chooseDateBtn.click();
        String dayOfWeek = date.format(DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH));
        String monthDay  = date.format(DateTimeFormatter.ofPattern("MMMM d", Locale.ENGLISH));
        String suffix    = ordinal(date.getDayOfMonth());
        String optionName = String.format("Choose %s, %s%s,", dayOfWeek, monthDay, suffix);
        Locator option = page.getByRole(AriaRole.OPTION,
                new Page.GetByRoleOptions().setName(optionName));
        waitForVisible(option);
        option.click();
        return this;
    }

    // ─── Suppliers section ────────────────────────────────────────────────────

    public PurchaseOrdersPage selectSecondReactSelectByTyping(String value) {
        Locator last = reactSelectInputs.last();
        waitForVisible(last);
        last.click();
        last.fill(value);
        page.waitForTimeout(TestEnvironment.SEARCH_DELAY_MS);
        last.press("Enter");
        return this;
    }

    // ─── Additional Information ───────────────────────────────────────────────

    public PurchaseOrdersPage setComment(String comment) {
        waitForVisible(commentTextarea);
        commentTextarea.click();
        commentTextarea.fill(comment);
        return this;
    }

    // ─── Items section ────────────────────────────────────────────────────────

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

    // ─── Submit ───────────────────────────────────────────────────────────────

    public PurchaseOrdersPage createDraftPurchaseOrder() {
        createDraftBtn.click();
        return this;
    }

    public PurchaseOrdersPage updatePurchaseOrder() {
        waitForVisible(updatePurchaseOrderBtn);
        updatePurchaseOrderBtn.click();
        return this;
    }

    // ─── Row actions (list page) ──────────────────────────────────────────────

    public PurchaseOrdersPage openFirstRowActions() {
        waitForVisible(firstRowActionsBtn);
        firstRowActionsBtn.click();
        return this;
    }

    public PurchaseOrdersPage clickEditFromMenu() {
        waitForVisible(editButtonInMenu);
        editButtonInMenu.click();
        assertThat(numberInputInEditMode).isVisible();
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

    // ─── Assertions ───────────────────────────────────────────────────────────

    public PurchaseOrdersPage assertUpdateSuccess() {
        assertThat(alertToast).isVisible();
        return this;
    }

    public void assertPurchaseOrderListed(String poNumber, String supplierName) {
        assertThat(page.locator("#root")).containsText(poNumber);
        assertThat(page.locator("#root")).containsText(supplierName);
    }

    public void assertPurchaseOrderListed(String poNumber) {
        assertThat(page.locator("#root")).containsText(poNumber);
    }

    public void assertPurchaseOrderNotListed(String poNumber) {
        assertThat(page.locator("#root")).not().containsText(poNumber);
    }

    // ─── Negative-test assertions ─────────────────────────────────────────────

    /** Asserts the "Create Draft Purchase Order" button is disabled (no items added). */
    public void assertCreateDraftButtonIsDisabled() {
        assertThat(createDraftBtn).isDisabled();
    }

    /** Asserts the "Create Draft Purchase Order" button is enabled (at least one item present). */
    public void assertCreateDraftButtonIsEnabled() {
        assertThat(createDraftBtn).isEnabled();
    }

    /** Asserts Ship To field shows "Field is required" validation error. */
    public void assertShipToValidationError() {
        assertThat(shipToValidationError).isVisible();
        assertThat(shipToValidationError).hasText("Field is required");
    }

    /** Asserts Required By date field shows "Enter a valid date" validation error. */
    public void assertRequiredByValidationError() {
        assertThat(requiredByValidationError).isVisible();
        assertThat(requiredByValidationError).hasText("Enter a valid date");
    }

    /** Asserts Supplier field shows "Field is required" validation error. */
    public void assertSupplierValidationError() {
        assertThat(supplierValidationError).isVisible();
        assertThat(supplierValidationError).hasText("Field is required");
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

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