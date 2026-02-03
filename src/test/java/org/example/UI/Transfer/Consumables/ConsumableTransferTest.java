package org.example.UI.Transfer.Consumables;

import org.assertj.core.api.Assertions;
import org.example.BaseUITestExtension.PlaywrightUiLoginBaseTest;
import org.example.UI.PageObjectModels.Alerts.AlertUtils;
import org.example.UI.PageObjectModels.Consumable.ConsumableListPage;
import org.example.UI.PageObjectModels.Stock.Warehouse.WarehousePage;
import org.example.UI.PageObjectModels.Transfer.TransferModalPage;
import org.example.UI.PageObjectModels.Transfer.TransferPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

public class ConsumableTransferTest extends PlaywrightUiLoginBaseTest {
    TransferPage transferPage;
    TransferModalPage modalPage;
    WarehousePage warehousePage;
    ConsumableListPage consumablePage;

    private final String warehouseToTransfer = "WarehouseToTransfer";
    private final Integer qtyForTransfer = 5;

    @BeforeEach
    public void setUp() {
        openPath("/stock/warehouse/warehousemain/ac1f56fd-9919-137e-8199-1f504b6607e8");
        transferPage = new TransferPage(page);
        warehousePage = new WarehousePage(page);
        consumablePage = new ConsumableListPage(page);
    }

    @DisplayName("Move Consumable to another Warehouse")
    @Order(0)
    @Test
    public void transferConsumableToAnotherWarehouse() {
        warehousePage.clickOnConsumablesTabButton();

        String consName = consumablePage.getFirstConsumableNameInTheList();
        Double consQty = consumablePage.getQtyFromConsumableLocationStock();

        transferPage.checkFirstRow();

        modalPage = transferPage.clickTransferButton();
        modalPage.waitForLoaded();


        modalPage.selectDestinationForConsumable(warehouseToTransfer);
        modalPage.setQtyForConsumable(qtyForTransfer);

        modalPage.clickTransfer();

        AlertUtils.waitForAlertVisible(page);
        String alertText = AlertUtils.getAlertText(page);
        org.assertj.core.api.Assertions.assertThat(alertText).isEqualTo("Consumable transfer was successful");
        AlertUtils.waitForAlertHidden(page);

        Assertions.assertThat(consumablePage.getQtyFromConsumableLocationStock())
                .isEqualTo(consQty - qtyForTransfer);
    }
}
