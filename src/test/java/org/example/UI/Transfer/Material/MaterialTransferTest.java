package org.example.UI.Transfer.Material;

import com.microsoft.playwright.assertions.PlaywrightAssertions;
import org.assertj.core.api.Assertions;
import org.example.BaseUITestExtension.PlaywrightUiLoginBaseTest;
import org.example.UI.PageObjectModels.Alerts.AlertUtils;
import org.example.UI.PageObjectModels.Material.MaterialsListPage;
import org.example.UI.PageObjectModels.Transfer.TransferModalPage;
import org.example.UI.PageObjectModels.Transfer.TransferPage;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MaterialTransferTest extends PlaywrightUiLoginBaseTest {
    MaterialsListPage materialsListPage;
    TransferModalPage modalPage;
    TransferPage transferPage;


    @BeforeEach
    public void setUp() {
        openPath("/stock/warehouse/warehousemain/ac1f56fd-9919-137e-8199-1f504b6607e8");
        materialsListPage = new MaterialsListPage(page);
        materialsListPage.waitFirstRowVisible();
        transferPage = new TransferPage(page);
    }

    @DisplayName("Transfer material from warehouse WarehouseMain to WarehouseToTransfer (set 100 → transfer 50)")
    @Order(0)
    @Test
    public void transferMaterialTest() {
        // 1) виставити кількість першого матеріалу в гріді = 100
        materialsListPage.setFirstRowGridQuantity(100);
        Assertions.assertThat(materialsListPage.getFirstRowQuantity()).isEqualTo(100);

        // чекнемо перший матеріал
        transferPage.waitForReady();
        transferPage.checkFirstRow();

// відкриваємо модалку “Transfer”
        modalPage = transferPage.clickTransferButton();

        // 4) у модалці: обрати локацію, вказати qty=50, натиснути Transfer → Confirm
        modalPage.selectLocation("WarehouseToTransfer");

        modalPage.setTransferQuantity(50);
        modalPage.clickTransfer();
        modalPage.clickConfirm();

        // 5) алерт про успіх
        AlertUtils.waitForAlertVisible(page);
        String alert = AlertUtils.getAlertText(page);
        PlaywrightAssertions.assertThat(page.getByRole(com.microsoft.playwright.options.AriaRole.ALERT))
                .containsText("successfully transferred");
        AlertUtils.waitForAlertHidden(page);

        // 6) перевірка, що у гріді тепер показується 50 (100 - 50)
        Assertions.assertThat(materialsListPage.getFirstRowQuantity()).isEqualTo(50);
    }
}
