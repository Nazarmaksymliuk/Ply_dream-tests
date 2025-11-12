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

    String jobName = "MainJob";
    String inUseStatus = "In Use";

    private final String warehouseToTransfer = "WarehouseToTransfer";

    @BeforeEach
    public void setUp() {
        openPath("/stock/warehouse/warehousemain/ac1f56fd-9919-137e-8199-1f504b6607e8");
        transferPage = new TransferPage(page);
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

    @DisplayName("Transfer tool from warehouse WarehouseMain to JobMain")
    @Order(1)
    @Test
    public void transferToolFromLocationToJobTest() {
        // Відкрити вкладку інструментів
        warehousePage.clickOnToolsTabButton();

        // Запам’ятати назву першого інструменту (щоб перевірити, що він зникне зі складу)
        String toolName = toolsListPage.getFirstToolNameInTheList();

        // 1) Вибрати перший рядок (інструмент)
        transferPage.checkFirstRow();

        // 2) Відкрити модалку "Transfer"
        modalPage = transferPage.clickMoveToJobButton();
        modalPage.waitForLoaded();

        // 4) Обрати Job, куди переносимо
        modalPage.setJobToTransfer(jobName);

        // 5) Натиснути "Transfer" і підтвердити
        modalPage.clickMoveTool();

        // 6) Перевірка: алерт успіху
        AlertUtils.waitForAlertVisible(page);
        String alertText = AlertUtils.getAlertText(page);
        Assertions.assertThat(alertText)
                .isEqualTo("Tools have been successfully moved");
        AlertUtils.waitForAlertHidden(page);

        // 7) Перевірка: інструмент більше не відображається в списку інструментів складу
        Assertions.assertThat(toolsListPage.getFirstToolJobName()).contains(jobName);
        Assertions.assertThat(toolsListPage.getFirstToolInUseStatusName()).contains(inUseStatus);

    }

}
