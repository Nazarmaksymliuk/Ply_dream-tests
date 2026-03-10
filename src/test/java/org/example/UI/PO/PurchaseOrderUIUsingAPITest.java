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
public class PurchaseOrderUIUsingAPITest extends PlaywrightUiApiBaseTest {

    private PurchaseOrdersPage po;

    // Test data — created via API in @BeforeAll
    private String warehouseName;
    private String supplierName;
    private WarehouseApiFixture warehouseFixture;
    private SupplierApiFixture supplierFixture;

    // PO identifiers
    private final String poNumber      = "AQA-PO-" + new Random().nextInt(100000);
    private final String editedPoNumber = "UPD-AQA-PO-" + new Random().nextInt(100000);

    //  NEW: comments used in create / edit assertions
    private final String poComment      = "AQA test comment for PO";
    private final String editedPoComment = "Updated AQA PO comment";

    // ─── Setup / Teardown ────────────────────────────────────────────────────

    @BeforeAll
    void beforeAll() throws IOException {
        warehouseFixture = WarehouseApiFixture.create(userApi)
                .provisionWarehouse("UI-PO Warehouse ");
        supplierFixture  = SupplierApiFixture.create(userApi)
                .provisionSupplier("UI-PO Supplier ");

        warehouseName = warehouseFixture.warehouseName();
        supplierName  = supplierFixture.supplierName();
    }

    @AfterAll
    void afterAll() {
        if (supplierFixture  != null) supplierFixture.cleanup("Cleanup after UI-PO test");
        if (warehouseFixture != null) warehouseFixture.cleanup("Cleanup after UI-PO test");
    }

    @BeforeEach
    void setUp() {
        openPath("/purchase-orders");
        po = new PurchaseOrdersPage(page);
    }

    // ─── Tests ───────────────────────────────────────────────────────────────

    @Test
    @Order(0)
    @DisplayName("Create Draft PO: number, location, required-by date, supplier, qty, comment")
    void createDraftPurchaseOrder_success() {
        po.startCreateNew()
                .chooseNewPO()
                // Order Details
                .setNumber(poNumber)
                // Shipping: location + auto-fill shipping address from it
                .selectFirstReactSelectByTyping(warehouseName)
                .setShipToAsShippingAddress()
                // Required By date
                .chooseNeedByDate(LocalDate.now())
                // Supplier
                .selectSecondReactSelectByTyping(supplierName)
                // NEW: Comment
                .setComment(poComment)
                // Items
                .addItem()
                .selectFirstMaterialRowAndAdd()
                .setQty("10")
                // Submit
                .createDraftPurchaseOrder();

        po.assertPurchaseOrderListed(poNumber,supplierName);
    }

    @Test
    @Order(1)
    @DisplayName("Edit PO: update number, qty and comment")
    void editPurchaseOrder_changeNumberAndQty_success() {
        po.openFirstRowActions()
                .clickEditFromMenu()
                // Update PO Number
                .setNumberInEditMode(editedPoNumber)
                // Update Qty
                .setQty("100")
                // NEW: Update comment
                .setComment(editedPoComment)
                // Submit
                .updatePurchaseOrder();

        po.assertPurchaseOrderListed(editedPoNumber, supplierName);
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