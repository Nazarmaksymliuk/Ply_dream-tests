package org.example.UI.PO;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.APIResponse;
import org.example.Api.helpers.LocationsHelper.LocationsClient;
import org.example.Api.helpers.SupplierHelper.SuppliersClient;
import org.example.BaseUIApiExtension.PlaywrightUiApiBaseTest;
import org.example.UI.PageObjectModels.PO.PurchaseOrdersPage;
import org.example.apifactories.LocationsTestDataFactory;
import org.example.apifactories.SuppliersTestDataFactory;
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

    @BeforeAll
    void beforeAll_createWarehouseAndSupplier() throws IOException {
        locationsClient = new LocationsClient(userApi);
        suppliersClient = new SuppliersClient(userApi);

        // 1) CREATE WAREHOUSE
        Map<String, Object> whBody =
                LocationsTestDataFactory.buildCreateWarehouseBody("UI-PO Warehouse ");

        APIResponse whResponse = locationsClient.createLocation(whBody, false);

        System.out.println("BEFOREALL CREATE WAREHOUSE status: " + whResponse.status());
        System.out.println("BEFOREALL CREATE WAREHOUSE body: " + whResponse.text());

        Assertions.assertTrue(whResponse.status() == 201 || whResponse.status() == 200);

        JsonNode createdWh = locationsClient.parseLocation(whResponse);
        warehouseId = createdWh.get("id").asText();
        warehouseName = createdWh.get("name").asText();

        Assertions.assertNotNull(warehouseId);
        Assertions.assertFalse(warehouseId.isBlank());
        Assertions.assertNotNull(warehouseName);
        Assertions.assertFalse(warehouseName.isBlank());

        // 2) CREATE SUPPLIER
        Map<String, Object> supBody = SuppliersTestDataFactory.buildCreateSupplierRequest(
                "UI-PO Supplier "
        );

        System.out.println("BEFOREALL CREATE SUPPLIER request body: " + supBody);

        APIResponse supResponse = suppliersClient.createSupplier(supBody);

        System.out.println("BEFOREALL CREATE SUPPLIER status: " + supResponse.status());
        System.out.println("BEFOREALL CREATE SUPPLIER body: " + supResponse.text());

        Assertions.assertTrue(supResponse.status() == 201 || supResponse.status() == 200);

        supplierId = suppliersClient.extractSupplierId(supResponse);
        Assertions.assertNotNull(supplierId);
        Assertions.assertFalse(supplierId.isBlank());

        // supplierName: беремо з response, якщо є; інакше — з body
        try {
            JsonNode supplierNode = suppliersClient.extractSupplierNode(supResponse);
            supplierName = supplierNode != null && supplierNode.get("businessName") != null
                    ? supplierNode.get("businessName").asText()
                    : (String) supBody.get("businessName");
        } catch (Exception e) {
            supplierName = (String) supBody.get("businessName");
        }

        Assertions.assertNotNull(supplierName);
        Assertions.assertFalse(supplierName.isBlank());
    }

    @AfterAll
    void afterAll_cleanupSupplierAndWarehouse() {
        // delete supplier first (часто PO може референсити supplier; якщо буде 409 — тоді після delete PO)
        if (supplierId != null && !supplierId.isBlank()) {
            try {
                APIResponse delSup = suppliersClient.deleteSupplier(supplierId);
                System.out.println("AFTERALL DELETE SUPPLIER status: " + delSup.status());
                System.out.println("AFTERALL DELETE SUPPLIER body: " + delSup.text());
                Assertions.assertTrue(delSup.status() == 204 || delSup.status() == 200);
            } catch (Exception e) {
                System.out.println("AFTERALL DELETE SUPPLIER failed: " + e.getMessage());
            }
        }

        if (warehouseId != null && !warehouseId.isBlank()) {
            try {
                APIResponse delLoc = locationsClient.deleteLocation(
                        warehouseId,
                        null,
                        "Delete warehouse via UI-PO test cleanup"
                );
                System.out.println("AFTERALL DELETE WAREHOUSE status: " + delLoc.status());
                System.out.println("AFTERALL DELETE WAREHOUSE body: " + delLoc.text());
                Assertions.assertTrue(delLoc.status() == 204 || delLoc.status() == 200);
            } catch (Exception e) {
                System.out.println("AFTERALL DELETE WAREHOUSE failed: " + e.getMessage());
            }
        }
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
