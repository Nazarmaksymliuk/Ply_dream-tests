package org.example.UI.PageObjectModels.Kits.KitsCreationFlow;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

import org.example.UI.PageObjectModels.Utils.LocationSelect;

public class KitStockSetupPage {
    private final Page page;

    // === Barcode section ===
    private final Locator barcodeInput;
    private final Locator generateCodeButton;
    private final Locator addCodeButton;

    // === Location section ===
    private final Locator locationDropdown;
    private final Locator addLocationButton;
    private final Locator deleteLocationButton;

    // === Position inputs ===
    private final Locator aisleInput;
    private final Locator bayInput;
    private final Locator levelInput;
    private final Locator binInput;

    // === Navigation buttons ===
    private final Locator nextButton;
    private final Locator previousButton;

    // === Utility for selecting locations ===
    private final LocationSelect locationSelectByEnter;


    public KitStockSetupPage(Page page) {
        this.page = page;

        // === Barcode ===
        barcodeInput = page.getByPlaceholder("Enter a 4-16 character code");
        generateCodeButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Generate Code"));
        addCodeButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Code"));

        // === Location ===
        locationDropdown = page.locator("div").filter(new Locator.FilterOptions().setHasText("Choose a location for the kit")).locator("..").locator("input");
        addLocationButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add location (optional)"));
        deleteLocationButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Delete location").setExact(false));

        // === Position at Location ===
        aisleInput = page.getByPlaceholder("A - Aisle (optional)");
        bayInput = page.getByPlaceholder("B - Bay (optional)");
        levelInput = page.getByPlaceholder("L - Level (optional)");
        binInput = page.getByPlaceholder("B - Bin (optional)");

        // === Navigation ===
        nextButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Next"));
        previousButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Previous"));

        // === Utility ===
        locationSelectByEnter = new LocationSelect(page);
    }

    // === Barcode Methods ===
    public void enterBarcode(String code) {
        barcodeInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        barcodeInput.fill(code);
    }

    public void clickGenerateCode() {
        generateCodeButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        generateCodeButton.click();
    }

    public void clickAddCode() {
        addCodeButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        addCodeButton.click();
    }

    // === Location Methods ===
    public void clickAddLocation() {
        addLocationButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        addLocationButton.click();
    }

    public void setWarehouseUsingUtility(String warehouseName) {
        locationSelectByEnter.setLocationByEnter(warehouseName);
    }

    public void deleteLocation() {
        deleteLocationButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        deleteLocationButton.click();
    }

    // === Position at Location ===
    public void setAisle(String aisle) {
        aisleInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        aisleInput.fill(aisle);
    }

    public void setBay(String bay) {
        bayInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        bayInput.fill(bay);
    }

    public void setLevel(String level) {
        levelInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        levelInput.fill(level);
    }

    public void setBin(String bin) {
        binInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        binInput.fill(bin);
    }

    // === Navigation ===
    public KitSettingsPage clickNext() {
        nextButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        nextButton.click();
        return new KitSettingsPage(page);
    }

    public void clickPrevious() {
        previousButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        previousButton.click();
    }
}
