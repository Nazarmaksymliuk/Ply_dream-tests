package org.example.UI.PageObjectModels.Stock;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.example.UI.PageObjectModels.Stock.Trucks.TrucksListPage;
import org.example.UI.PageObjectModels.Stock.Warehouse.WarehousesListPage;

public class StockPage {
    private final Page page;

    // === Локатори ===
    private final Locator warehousesTabButton;
    private final Locator trucksTabButton;

    private final Locator menuItemEdit;
    private final Locator templatesTab;


    // === Конструктор ===
    public StockPage(Page page) {
        this.page = page;

        warehousesTabButton = page.getByRole(
                AriaRole.TAB,
                new Page.GetByRoleOptions().setName("Warehouses")
        );



        trucksTabButton = page.getByRole(
                AriaRole.TAB,
                new Page.GetByRoleOptions().setName("Fleet"));


        menuItemEdit = page.getByRole(
                AriaRole.MENUITEM,
                new Page.GetByRoleOptions().setName("Edit") //// MAINNNNNNNNNN FIRSTTTTTTTTT
        )
                .first();

        this.templatesTab = page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("Templates"));


    }



    public WarehousesListPage clickWarehousesTabButton() {
        warehousesTabButton.waitFor(
                new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE)
        );
        warehousesTabButton.click();
        return new WarehousesListPage(page);
    }
    public TrucksListPage clickOnTruckTabButton() {
        trucksTabButton.waitFor(
                new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE)
        );
        trucksTabButton.click();
        return new TrucksListPage(page);
    }

    public Locator warehousesTabButton(){
        return warehousesTabButton;
    }

    public <T> T chooseMenuActionEdit(Class<T> returnPageClass) {
        menuItemEdit.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        menuItemEdit.click();
        try {
            return returnPageClass.getDeclaredConstructor(Page.class).newInstance(page);
        } catch (Exception e) {
            throw new RuntimeException("Не вдалося створити екземпляр сторінки: " + returnPageClass.getSimpleName(), e);
        }
    }

    public void clickOnTemplatesTab() {
        templatesTab.click();
    }

    public Locator templatesTabButton() {
        return templatesTab;
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
