package org.example.UI.Transfer.Kits;

import org.example.BaseUITestExtension.PlaywrightUiLoginBaseTest;
import org.example.UI.PageObjectModels.Alerts.AlertUtils;
import org.example.UI.PageObjectModels.Kits.KitsListPage;
import org.example.UI.PageObjectModels.Stock.Warehouse.WarehousePage;
import org.example.UI.PageObjectModels.Transfer.TransferModalPage;
import org.example.UI.PageObjectModels.Transfer.TransferPage;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

public class KitTransferTest extends PlaywrightUiLoginBaseTest {
    TransferPage transferPage;
    TransferModalPage modalPage;
    WarehousePage warehousePage;
    KitsListPage kitsPage;

    private final String warehouseToTransfer = "WarehouseToTransfer";

    @BeforeEach
    public void setUp() {
        openPath("/stock/warehouse/warehousemain/ac1f56fd-9919-137e-8199-1f504b6607e8");
        transferPage = new TransferPage(page);
        warehousePage = new WarehousePage(page);
        kitsPage = new KitsListPage(page);
    }

    @DisplayName("Move Kit to another Warehouse")
    @Order(0)
    @Test
    public void transferKitToAnotherWarehouse() {
        warehousePage.clickOnKitsTabButton();

        String kitName = kitsPage.getFirstKitNameInTheList();
        // 1️⃣ Вибрати чекбокс у першому рядку
        transferPage.checkFirstRow();

        modalPage = transferPage.clickTransferButton();
        modalPage.waitForLoaded();

        modalPage.clickLocationModalButton();
        // 3️⃣ Обрати цільовий склад
        modalPage.selectLocation(warehouseToTransfer);
        modalPage.setQtyForMaterialInKit(10);
        // 4️⃣ Натиснути “Move 1 Tool”
        modalPage.clickTransfer();
        modalPage.clickTransfer();

        // 6️⃣ Перевірка: алерт успішного трансферу
        AlertUtils.waitForAlertVisible(page);
        String alertText = AlertUtils.getAlertText(page);
        org.assertj.core.api.Assertions.assertThat(alertText).contains("successfully transferred");
        AlertUtils.waitForAlertHidden(page);

    }

}
