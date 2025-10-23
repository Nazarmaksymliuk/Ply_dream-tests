package org.example.PageObjectModels.Catalog.ToolsTab;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import net.bytebuddy.asm.Advice;
import org.example.components.LocationSelect;

import java.util.List;

public class AddUnitsPage {
    private final Page page;

    // === Detailed Information ===
    private final Locator unitNameInput;          // "Enter unit name"
    private final Locator serialInput;            // "Enter serial number"
    private final Locator statusSelect;           // "Select status" (react-select)
    private final Locator employeeSelect;         // "Select employee" (react-select)

    // === Location and Job ===
    private final Locator locationSelect;         // "Select location" (react-select)
    private final Locator jobsSelect;             // "Select jobs" (react-select, multi)

    // === Position at Location ===
    private final Locator aisleInput;             // "A - Aisle (optional)"
    private final Locator bayInput;               // "B - Bay (optional)"
    private final Locator levelInput;             // "L - Level (optional)"
    private final Locator binInput;               // "B - Bin (optional)"

    // === Location Barcodes / Data ===
    private final Locator dataCodeInput;          // "Enter a 4-16 character code"
    private final Locator generateCodeButton;     // "Generate Code"
    private final Locator addCodeButton;          // "+ Add Code"

    // === Additional Information ===
    private final Locator supplierSelect;         // "Select supplier" (react-select)
    private final Locator dateOfPurchaseInput;    // "Select date of purchase"
    private final Locator purchaseCostInput;      // "Enter purchase cost"
    private final Locator valueInput;             // "Enter value"
    private final Locator notesInput;             // "Enter notes"
    private final Locator warehouseDropdown;
    private final Locator warehouseInput;

    private final Locator saveInformationButton;
    private final Locator saveButton;

    private final LocationSelect locationSelectByEnter;


    public AddUnitsPage(Page page) {
        this.page = page;

        // --- inputs by placeholder ---
        unitNameInput       = page.getByPlaceholder("Enter unit name");
        serialInput         = page.getByPlaceholder("Enter serial number");

        statusSelect        = page.getByPlaceholder("Select status")
                .or(page.getByText("Select status").first());
        employeeSelect      = page.getByPlaceholder("Select employee")
                .or(page.getByText("Select employee").first());

        locationSelect      = page.getByPlaceholder("Select location")
                .or(page.getByText("Select location").first());
        jobsSelect          = page.getByPlaceholder("Select jobs")
                .or(page.getByText("Select jobs").first());

        aisleInput          = page.getByPlaceholder("A - Aisle (optional)");
        bayInput            = page.getByPlaceholder("B - Bay (optional)");
        levelInput          = page.getByPlaceholder("L - Level (optional)");
        binInput            = page.getByPlaceholder("B - Bin (optional)");

        dataCodeInput       = page.getByPlaceholder("Enter a 4-16 character code");
        generateCodeButton  = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Generate Code"));
        addCodeButton       = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Code"))
                .or(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("+ Add Code")));

        supplierSelect      = page.getByPlaceholder("Select supplier")
                .or(page.getByText("Select supplier").first());
        dateOfPurchaseInput = page.getByPlaceholder("Select date of purchase");
        purchaseCostInput   = page.getByPlaceholder("Enter purchase cost");
        valueInput          = page.getByPlaceholder("Enter value");
        notesInput          = page.getByPlaceholder("Enter notes");

        saveInformationButton = page.getByRole(
                AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save Information")
        );

        saveButton = page.locator("[class^=_actions_]").getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Save"));


        warehouseDropdown = page.locator("div.react_select__control");
        warehouseInput = page.locator("div.react_select__input-container input");



        locationSelectByEnter = new LocationSelect(page);
    }

    // ===== waits =====
    public void waitForLoaded() {
        unitNameInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(30000));
    }

    // ===== setters (inputs) =====
    public void setUnitName(String unitName) {
        unitNameInput.fill(unitName);
    }
    public void setSerialNumber(String serial) {
        serialInput.fill(serial);
    }
    public void setAisle(String aisle) {
        aisleInput.fill(aisle);
    }
    public void setBay(String bay) {
        bayInput.fill(bay);
    }
    public void setLevel(String level) {
        levelInput.fill(level);
    }
    public void setBin(String bin) {
        binInput.fill(bin);
    }
    public void setDataCode(String code) {
        dataCodeInput.fill(code);
    }
    public void setPurchaseCost(double cost) {
        purchaseCostInput.fill(String.valueOf(cost));
    }
    public void setValue(double value) {
        valueInput.fill(String.valueOf(value));
    }
    public void setNotes(String notes) {
        notesInput.fill(notes);
    }

    // ===== selects (react-select style) =====
    public void selectStatus(String statusText) {
        openAndChoose(statusSelect, statusText);
    }

    public void selectFirstStatus(){
        statusSelect.click();
        page.keyboard().press("Enter");
    }

    public void setWarehouseUsingUtility(String warehouseName){
        locationSelectByEnter.setLocationByEnter(warehouseName);
    }

    public void selectEmployee(String employeeText) {
        openAndChoose(employeeSelect, employeeText);
    }
    public void selectLocation(String locationText) {
        openAndChoose(locationSelect, locationText);
    }
    public void selectJob(String jobText) {
        openAndChoose(jobsSelect, jobText);
    }
    /** Для мультивибору робіт — виклич кілька разів */
    public void selectJobs(String... jobs) {
        for (String j : jobs) selectJob(j);
    }
    public void selectSupplier(String supplierText) {
        openAndChoose(supplierSelect, supplierText);
    }

    public void clickSaveInformationButton(){
        saveInformationButton.click();
    }
    public void clickSaveButton(){
        saveButton.click();
    }

    // ===== helper for react-select dropdowns =====
    private void openAndChoose(Locator select, String optionText) {
        select.click(); // відкрити список
        // загальний селектор опцій react-select
        Locator option = page.locator(".react_select__option, .react-select__option, [role='option']")
                .filter(new Locator.FilterOptions().setHasText(optionText))
                .first();
        option.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(10000));
        option.click();
    }
}
