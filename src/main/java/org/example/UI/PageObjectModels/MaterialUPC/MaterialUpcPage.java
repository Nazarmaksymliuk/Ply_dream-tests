package org.example.UI.PageObjectModels.MaterialUPC;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.example.UI.PageObjectModels.Utils.Waits.WaitUtils;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class MaterialUpcPage {

    private final Page page;

    // кнопка з іконкою AddCircleOutlineIcon і текстом "Manage UPC"
    private final Locator manageUpcBtn;

    // кнопка "Add Supplier UPC"
    private final Locator addSupplierUpcBtn;

    // react-select control
    private final Locator supplierSelectControl;
    private final Locator supplierSelectInput;

    // поле UPC
    private final Locator upcInput;

    // Save
    private final Locator saveBtn;
    private final Locator upcArrowButton;

    private final Locator deleteUpcIcon;
    private final Locator confirmDeleteBtn;

    public MaterialUpcPage(Page page) {
        this.page = page;

        // 1) AddCircleOutlineIcon + текст Manage UPC (страхуємось двома умовами)
        this.manageUpcBtn = page.locator("[data-testid='AddCircleOutlineIcon']").filter(
                new Locator.FilterOptions().setHasText("Manage UPC")
        );

        // якщо текст не всередині цього ж елемента (часто так), fallback:
        // кнопка з іконкою + поруч текст
        // (залишаємо як запасний варіант в методі clickManageUpc)

        this.addSupplierUpcBtn = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Add Supplier UPC"));

        this.supplierSelectControl = page.getByTestId("ply_select_component_control");
        this.supplierSelectInput = supplierSelectControl.locator("input");

        this.upcInput = page.getByPlaceholder("Enter UPC code");
        this.saveBtn = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save"));

        this.upcArrowButton = page.getByTestId("KeyboardArrowDownIcon");

        this.deleteUpcIcon = page.getByTestId("DeleteOutlineOutlinedIcon").first();

        this.confirmDeleteBtn = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Delete")
        );
    }

    public MaterialUpcPage clickManageUpc() {
        WaitUtils.waitForLoaderToDisappear(page);

        if (manageUpcBtn.count() > 0) {
            manageUpcBtn.first().click();
            return this;
        }

        // ✅ fallback: знайти блок з текстом "Manage UPC" і в ньому клікнути іконку
        Locator container = page.getByText("Manage UPC").first();
        Locator iconInside = container.locator("xpath=ancestor-or-self::*[1]").locator("[data-testid='AddCircleOutlineIcon']").first();

        // якщо іконка не “всередині” цього ж вузла, беремо найближчого батька вище (часто треба)
        if (iconInside.count() == 0) {
            iconInside = container.locator("xpath=ancestor::*[self::button or self::div][1]")
                    .locator("[data-testid='AddCircleOutlineIcon']").first();
        }

        assertThat(iconInside).isVisible();
        iconInside.click();

        return this;
    }

    public MaterialUpcPage editUpcCode(int newUpc) {
        WaitUtils.waitForLoaderToDisappear(page);

        assertThat(upcInput).isVisible();
        upcInput.click();

        // ✅ надійно чистимо
        upcInput.fill("");
        upcInput.fill(String.valueOf(newUpc));

        return this;
    }

    public MaterialUpcPage deleteUpc() {
        WaitUtils.waitForLoaderToDisappear(page);

        assertThat(deleteUpcIcon).isVisible();
        deleteUpcIcon.click();

        // confirmation modal
        assertThat(confirmDeleteBtn).isVisible();
        confirmDeleteBtn.click();

        WaitUtils.waitForLoaderToDisappear(page);
        page.waitForTimeout(2000);
        return this;
    }

    public MaterialUpcPage deleteUpcByValue(int upc) {
        Locator row = page.locator("tr")
                .filter(new Locator.FilterOptions().setHasText(String.valueOf(upc)))
                .first();

        Locator deleteBtn = row.locator("[data-testid='DeleteOutlineOutlinedIcon']").first();

        assertThat(deleteBtn).isVisible();
        deleteBtn.click();

        assertThat(confirmDeleteBtn).isVisible();
        confirmDeleteBtn.click();

        return this;
    }

    public MaterialUpcPage clickAddSupplierUpc() {
        WaitUtils.waitForLoaderToDisappear(page);
        assertThat(addSupplierUpcBtn).isVisible();
        addSupplierUpcBtn.click();
        return this;
    }

    public MaterialUpcPage selectSupplierByName(String supplierName) {
        WaitUtils.waitForLoaderToDisappear(page);

        assertThat(supplierSelectControl).isVisible();
        supplierSelectControl.click();

        // react-select input інколи зʼявляється після кліку
        assertThat(supplierSelectInput).isVisible();
        supplierSelectInput.fill(supplierName);

        // як ти сказав: Enter через ~2 секунди
        page.waitForTimeout(2000);
        supplierSelectInput.press("Enter");

        return this;
    }

    public MaterialUpcPage enterUpcCode(int upc) {
        WaitUtils.waitForLoaderToDisappear(page);

        assertThat(upcInput).isVisible();
        upcInput.click();
        upcInput.fill(String.valueOf(upc));

        return this;
    }

    public MaterialUpcPage clickSave() {
        WaitUtils.waitForLoaderToDisappear(page);

        assertThat(saveBtn).isVisible();
        saveBtn.click();

        WaitUtils.waitForLoaderToDisappear(page);
        return this;
    }

    public MaterialUpcPage openUpcList(){
        WaitUtils.waitForLoaderToDisappear(page);
        upcArrowButton.click();
        return this;
    }

    public void assertUpcNotVisible(int upc) {
        String upcText = String.valueOf(upc);
        WaitUtils.waitForLoaderToDisappear(page);

        // якщо список відкритий — так стабільніше:
        assertThat(page.locator("text=" + upcText)).isHidden();
    }

    // optional: швидка перевірка, що UPC зʼявився на сторінці
    public void assertUpcVisible(int upc) {
        assertThat(page.locator("#root"))
                .containsText(String.valueOf(upc));
    }
}