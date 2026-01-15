package org.example.UI.PO;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.APIResponse;
import org.example.Api.helpers.LocationsHelper.LocationsClient;
import org.example.Api.helpers.SupplierHelper.SuppliersClient;
import org.example.BaseUIApiExtension.PlaywrightUiApiBaseTest;
import org.example.UI.PageObjectModels.PO.PurchaseOrdersPage;
import org.example.apifactories.LocationsTestDataFactory;
import org.example.apifactories.SuppliersTestDataFactory;
import org.example.fixtures.SupplierApiFixture;
import org.example.fixtures.WarehouseApiFixture;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.Random;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PurchaseOrderUIUsingAPITest extends PlaywrightUiApiBaseTest {

    private PurchaseOrdersPage po;

    private LocationsClient locationsClient;
    private SuppliersClient suppliersClient;

    private String warehouseId;
    private String warehouseName;

    private String supplierId;
    private String supplierName;

    private final String poNumber = "AQA-PO-" + new Random().nextInt(100000);
    private final String editedPoNumber = "UPD-AQA-PO-" + new Random().nextInt(100000);

    private WarehouseApiFixture warehouseFixture;
    private SupplierApiFixture supplierFixture;

    @BeforeAll
    void beforeAll() throws IOException {
        warehouseFixture = WarehouseApiFixture.create(userApi).provisionWarehouse("UI-PO Warehouse ");
        supplierFixture  = SupplierApiFixture.create(userApi).provisionSupplier("UI-PO Supplier ");

        warehouseId = warehouseFixture.warehouseId();
        warehouseName = warehouseFixture.warehouseName();

        supplierId = supplierFixture.supplierId();
        supplierName = supplierFixture.supplierName();
    }

    @AfterAll
    void afterAll() {
        if (supplierFixture != null) supplierFixture.cleanup("Cleanup after UI-PO test");
        if (warehouseFixture != null) warehouseFixture.cleanup("Cleanup after UI-PO test");
    }

    @BeforeEach
    void setUp() {
        openPath("/purchase-orders");
        po = new PurchaseOrdersPage(page);
    }

    @Test
    @Order(0)
    @DisplayName("Create Draft Purchase Order (warehouse + supplier created via API)")
    void createDraftPurchaseOrder_success() {

        po.startCreateNew()
                .chooseNewPO()
                .setNumber(poNumber)
                .selectFirstReactSelectByTyping(warehouseName)   // Ship To / Location
                .setShipToAsShippingAddress()
                .chooseNeedByDate(LocalDate.now())
                .selectSecondReactSelectByTyping(supplierName)   // ✅ supplier instead of "main"
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
                .setNumber(editedPoNumber)
                .setQty("100")
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
