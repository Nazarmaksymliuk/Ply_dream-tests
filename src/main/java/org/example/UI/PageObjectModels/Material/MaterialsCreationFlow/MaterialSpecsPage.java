package org.example.UI.PageObjectModels.Material.MaterialsCreationFlow;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

public class MaterialSpecsPage {
    private final Page page;

    // === Locators ===
    private final Locator materialNameInput;
    private final Locator itemNumberInput;
    private final Locator nextButton;
    private final Locator descriptionTextArea;
    private final Locator brandInput;
    private final Locator manufacturerInput;
    private final Locator categoryInput;
    private final Locator variantNameInput;
    private final Locator variantDescriptionInput;
    private final Locator addVariantButton;
    private final Locator saveButton;
    private final Locator serializedCheckBox;
    private final Locator addNewMaterialButton;
    private final Locator specialorderItemCheckBox;

    // === Constructor ===
    public MaterialSpecsPage(Page page) {
        this.page = page;

        materialNameInput = page.locator("input[placeholder='Enter material name']");
        itemNumberInput = page.locator("input[placeholder='Enter item #']");
        nextButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Next"));
        descriptionTextArea = page.locator("textarea[placeholder='Add material description']");
        brandInput = page.locator("input[placeholder='Enter brand']");
        manufacturerInput = page.locator("input[placeholder='Enter manufacturer']");
        categoryInput = page.locator("input[placeholder='Choose category']");
        variantNameInput = page.locator("input[placeholder='Enter name']");
        variantDescriptionInput = page.locator("input[placeholder='Enter description']");
        addVariantButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Material Variant"));
        saveButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save"));
        serializedCheckBox = page.getByTestId("ply_checkbox").nth(1);
        addNewMaterialButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add New Material"));
        specialorderItemCheckBox = page.getByTestId("ply_checkbox").nth(2);
    }

    // === Actions ===

    public void setMaterialName(String materialValue) {
        materialNameInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        materialNameInput.click();    // THIS CLICK IS NEEDED CAUSE PLACEHOLDER CLEARED AND THE NEW DATA IS NOT FILLED
        materialNameInput.fill(materialValue);

        clickOnButtonAddNewMaterial();
    }

    public void clickOnButtonAddNewMaterial() {
        addNewMaterialButton.click();
    }

    public Locator materialNameLocator() {
        return materialNameInput;
    }

    public void setItemNumber(String itemNumberValue) {
        itemNumberInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        itemNumberInput.fill(itemNumberValue);
    }

    public void checkSerializedCheckBox(){
        serializedCheckBox.click();
    }

    public void checkSpecialOrderItemCheckBox(){
        specialorderItemCheckBox.click();
    }


    public void setDescription(String descriptionValue) {
        descriptionTextArea.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        descriptionTextArea.fill(descriptionValue);
    }

    public void setBrand(String brandValue) {
        brandInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        brandInput.fill(brandValue);
    }

    public void setManufacturer(String manufacturerValue) {
        manufacturerInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        manufacturerInput.fill(manufacturerValue);
    }

    public void setCategory(String categoryValue) {
        categoryInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        categoryInput.fill(categoryValue);
    }

    public void clickAddMaterialVariantButton() {
        addVariantButton.scrollIntoViewIfNeeded();
        addVariantButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        addVariantButton.click();
    }

    public void setVariantName(String variantValue) {
        variantNameInput.scrollIntoViewIfNeeded();
        variantNameInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        variantNameInput.fill(variantValue);
    }

    public void setVariantDescription(String variantValue) {
        variantDescriptionInput.scrollIntoViewIfNeeded();
        variantDescriptionInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        variantDescriptionInput.fill(variantValue);
    }

    public PriceAndVariantsPage clickNextButton() {
        nextButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        nextButton.click();
        return new PriceAndVariantsPage(page);
    }


    public void clickSaveButtonInTheEditMaterialFlow(){
        saveButton.click();
    }
}
