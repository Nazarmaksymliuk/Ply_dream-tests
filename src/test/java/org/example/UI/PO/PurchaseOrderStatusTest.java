package org.example.UI.PO;

import org.example.BaseUIApiExtension.PlaywrightUiApiBaseTest;
import org.example.UI.PageObjectModels.PO.PurchaseOrdersPage;
import org.example.fixtures.SupplierApiFixture;
import org.example.fixtures.WarehouseApiFixture;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Random;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PurchaseOrderStatusTest extends PlaywrightUiApiBaseTest {

    private PurchaseOrdersPage po;

    private String warehouseName;
    private String supplierName;
    private WarehouseApiFixture warehouseFixture;
    private SupplierApiFixture supplierFixture;

    private final String poNumber = "STATUS-PO-" + new Random().nextInt(100000);

    // ─── Setup / Teardown ───────────────────────────────────────────────────

    @BeforeAll
    void beforeAll() throws IOException {
        warehouseFixture = WarehouseApiFixture.create(userApi)
                .provisionWarehouse("Status-PO Warehouse ");
        supplierFixture = SupplierApiFixture.create(userApi)
                .provisionSupplier("Status-PO Supplier ");

        warehouseName = warehouseFixture.warehouseName();
        supplierName  = supplierFixture.supplierName();
    }

    @AfterAll
    void afterAll() {
        if (supplierFixture  != null) supplierFixture.cleanup("Cleanup after PO status test");
        if (warehouseFixture != null) warehouseFixture.cleanup("Cleanup after PO status test");
    }

    @BeforeEach
    void setUp() {
        openPath("/purchase-orders");
        po = new PurchaseOrdersPage(page);
    }

    // ─── Tests ─────────────────────────────────────────────────────────────

    @Test
    @Order(0)
    @DisplayName("Precondition: Create Draft PO for status-flow tests")
    void createDraftPO_forStatusTests() {
        po.startCreateNew()
                .chooseNewPO()
                .setNumber(poNumber)
                .selectFirstReactSelectByTyping(warehouseName)
                .setShipToAsShippingAddress()
                .chooseNeedByDate(LocalDate.now())
                .selectSecondReactSelectByTyping(supplierName)
                .addItem()
                .selectFirstMaterialRowAndAdd()
                .setQty("5")
                .createDraftPurchaseOrder();

        po.assertPurchaseOrderListed(poNumber);

        // Перевіряємо на Board, що ПО відразу потрапило у колонку Draft
        //po.switchToBoardView();
        po.assertPOInStatusColumnOnBoard(poNumber, "Draft");
    }

    @Test
    @Order(1)
    @DisplayName("Change PO status: Draft → Waiting for approval; verify on Board")
    void changePOStatus_draftToWaitingForApproval() {
        po.openFirstRowActions()
                .clickChangeStatusFromMenu()
                .selectStatusInDialog("Waiting for approval")
                .confirmStatusChange();

        //po.switchToBoardView();
        po.assertPOInStatusColumnOnBoard(poNumber, "Waiting for approval");
    }

    @Test
    @Order(2)
    @DisplayName("Change PO status: Waiting for approval → Sent; verify on Board")
    void changePOStatus_waitingForApprovalToSent() {
        po.openFirstRowActions()
                .clickChangeStatusFromMenu()
                .selectStatusInDialog("Sent")
                .confirmStatusChangeAndDoNotSendToSupplier();

        //po.switchToBoardView();
        po.assertPOInStatusColumnOnBoard(poNumber, "Sent");
    }

    @Test
    @Order(3)
    @DisplayName("Change PO status: Sent → Confirmed; verify on Board")
    void changePOStatus_sentToConfirmed() {
        po.openFirstRowActions()
                .clickChangeStatusFromMenu()
                .selectStatusInDialog("Confirmed")
                .confirmStatusChange();

        //po.switchToBoardView();
        po.assertPOInStatusColumnOnBoard(poNumber, "Confirmed");
    }

    @Test
    @Order(4)
    @DisplayName("Change PO status: Confirmed → Received; verify on Board")
    void changePOStatus_confirmedToReceived() {
        po.openFirstRowActions()
                .clickChangeStatusFromMenu()
                .selectStatusInDialog("Received")
                .confirmStatusChange();

        //po.switchToBoardView();
        po.assertPOInStatusColumnOnBoard(poNumber, "Received");
    }

    @Test
    @Order(5)
    @DisplayName("Change PO status: Received → Archived; verify on Board")
    void changePOStatus_receivedToArchived() {
        po.openFirstRowActions()
                .clickChangeStatusFromMenu()
                .selectStatusInDialog("Archived")
                .confirmStatusChange();

        //po.switchToBoardView();
        po.assertPOInStatusColumnOnBoard(poNumber, "Archived");
    }
}