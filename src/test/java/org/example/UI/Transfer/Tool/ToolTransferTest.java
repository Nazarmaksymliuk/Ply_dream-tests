package org.example.UI.Transfer.Tool;

import org.assertj.core.api.Assertions;
import org.example.BaseUITestExtension.PlaywrightUiLoginBaseTest;
import org.example.UI.PageObjectModels.Stock.Warehouse.WarehousePage;
import org.example.UI.PageObjectModels.Tools.ToolsListPage;
import org.example.UI.PageObjectModels.Transfer.TransferModalPage;
import org.example.UI.PageObjectModels.Transfer.TransferPage;
import org.example.UI.PageObjectModels.Alerts.AlertUtils;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ToolTransferTest extends PlaywrightUiLoginBaseTest {

    TransferPage transferPage;
    TransferModalPage modalPage;
    WarehousePage warehousePage;
    ToolsListPage toolsListPage;

    private final String warehouseToTransfer = "WarehouseToTransfer";

    @BeforeEach
    public void setUp() {
        openPath("/stock/warehouse/warehousemain/ac1f56fd-9919-137e-8199-1f504b6607e8");
        transferPage = new TransferPage(page);
        transferPage.waitForReady();
        warehousePage = new WarehousePage(page);
        toolsListPage = new ToolsListPage(page);
    }

    @DisplayName("Move Tool to another Warehouse")
    @Order(0)
    @Test
    public void moveToolToAnotherWarehouse() {
        warehousePage.clickOnToolsTabButton();

        String toolName = toolsListPage.getFirstToolNameInTheList();
        // 1️⃣ Вибрати чекбокс у першому рядку
        transferPage.checkFirstRow();

        // 2️⃣ Відкрити модалку Move To Location
        modalPage = transferPage.clickMoveToLocationButton();
        modalPage.waitForLoaded();

        // 3️⃣ Обрати цільовий склад
        modalPage.selectLocation(warehouseToTransfer);

        // 4️⃣ Натиснути “Move 1 Tool”
        modalPage.clickMoveTool();

        // 6️⃣ Перевірка: алерт успішного трансферу
        AlertUtils.waitForAlertVisible(page);
        String alertText = AlertUtils.getAlertText(page);
        Assertions.assertThat(alertText).contains("successfully moved");
        AlertUtils.waitForAlertHidden(page);

        Assertions.assertThat(toolsListPage.getToolNamesList()).doesNotContain(toolName);
    }
}
