package org.example.PageObjectModels.Stock;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.example.PageObjectModels.Stock.Warehouse.WarehousesListPage;

public class StockPage {
    private final Page page;

    // === Локатори ===
    private final Locator warehousesTabButton;
    private final Locator trucksTabButton;

    // === Конструктор ===
    public StockPage(Page page) {
        this.page = page;

        warehousesTabButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Warehouses")
        );

        trucksTabButton = page.getByRole(
                AriaRole.TAB,
                new Page.GetByRoleOptions().setName("Fleet")
        ).or(page.getByText("Fleet"));
    }

    public WarehousesListPage clickWarehousesTabButton() {
        warehousesTabButton.waitFor(
                new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE)
        );
        warehousesTabButton.click();
        return new WarehousesListPage(page);
    }

    public Locator warehousesTabButton(){
        return warehousesTabButton;
    }



    // (опційно) Можеш додати вейт на завантаження сторінки
    public void waitForLoaded() {
        warehousesTabButton.waitFor(
                new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(15000)
        );
    }
}
