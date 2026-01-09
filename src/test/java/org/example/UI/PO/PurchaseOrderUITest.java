package org.example.UI.PO;

import org.example.BaseUIApiExtension.PlaywrightUiApiBaseTest;
import org.example.UI.PageObjectModels.PO.PurchaseOrdersPage;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.Random;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PurchaseOrderUITest extends PlaywrightUiApiBaseTest {

    PurchaseOrdersPage po;

    static String poNumber = "AQA-PO-" + new Random().nextInt(100000);
    static String editedPoNumber = "UPD-AQA-PO-" + new Random().nextInt(100000);

    @BeforeEach
    public void setUp() {
        openPath("/purchase-orders");
        po = new PurchaseOrdersPage(page);
    }

    @Test
    @Order(0)
    @DisplayName("Create Draft Purchase Order")
    void createDraftPurchaseOrder_success() {

        po.startCreateNew()
                .chooseNewPO()
                .setNumber(poNumber)
                .selectFirstReactSelectByTyping("main")
                .setShipToAsShippingAddress()
                .chooseNeedByDate(LocalDate.now())
                .selectSecondReactSelectByTyping("main")
                .addItem()
                .selectFirstMaterialRowAndAdd()
                .setQty("10")
                .createDraftPurchaseOrder();

        po.assertPurchaseOrderListed(poNumber);
    }

    @Test
    @Order(1)
    @DisplayName("Edit PO: change number and qty")
    void editPurchaseOrder_changeNumberAndQty_success() {

        po.openFirstRowActions()
                .clickEditFromMenu()
                .setNumber(editedPoNumber)     // міняємо номер
                .setQty("100")                 // міняємо кількість
                .updatePurchaseOrder();

        po.assertPurchaseOrderListed(editedPoNumber);
    }

    @Test
    @Order(2)
    @DisplayName("Delete PO")
    void deletePurchaseOrder_success() {

        po.openFirstRowActions()
                .clickDeleteFromMenu()
                .deletePurchaseOrder();

        po.assertPurchaseOrderNotListed(editedPoNumber);
    }
}
