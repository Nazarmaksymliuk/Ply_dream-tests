package org.example.UI.MaterialUPC;

import org.example.BaseUIApiExtension.PlaywrightUiApiBaseTest;
import org.example.UI.PageObjectModels.MaterialUPC.MaterialUpcPage;
import org.example.fixtures.SupplierApiFixture;
import org.example.fixtures.WarehouseMaterialApiFixture;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.Random;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MaterialSupplierUpcUiTest extends PlaywrightUiApiBaseTest {

    private SupplierApiFixture supplierFixture;
    private WarehouseMaterialApiFixture fixture;

    private String supplierName;

    private String warehouseId;
    private String warehouseName;

    private String materialId;
    private String materialVariationId;

    private MaterialUpcPage materialUpcPage;

    private final int upc = generateUpc();
    private final int editedUpc = generateUpc();

    @BeforeAll
    void beforeAll() throws IOException {
        supplierFixture = SupplierApiFixture.create(userApi).provisionSupplier("UI-PO Supplier ");
        supplierName = supplierFixture.supplierName();

        fixture = WarehouseMaterialApiFixture.create(userApi)
                .provisionWarehouseWithAttachedMaterial();

        warehouseId = fixture.warehouseId();
        warehouseName = fixture.warehouseName();
        materialId = fixture.materialId();
        materialVariationId = fixture.materialVariationId();
    }

    @AfterAll
    void afterAll() {
        if (fixture != null) fixture.cleanup();
        if (supplierFixture != null) supplierFixture.cleanup("Cleanup after UI-PO test");
    }

    @BeforeEach
    void setUp() {
        openPath("/material/" + materialId);
        materialUpcPage = new MaterialUpcPage(page);
    }

    @Test
    @Order(0)
    @DisplayName("Add Supplier UPC to Material")
    void addSupplierUpcToMaterial() {

        materialUpcPage
                .clickManageUpc()
                .clickAddSupplierUpc()
                .selectSupplierByName(supplierName)
                .enterUpcCode(upc)
                .clickSave()
                .openUpcList();

        materialUpcPage.assertUpcVisible(upc);
    }

    @Test
    @Order(1)
    @DisplayName("Edit UPC for Material (change UPC code only)")
    void editUpcForMaterial() {

        materialUpcPage
                .clickManageUpc()
                .editUpcCode(editedUpc)   // ✅ новий метод (без Add Supplier UPC)
                .clickSave()
                .openUpcList();

        materialUpcPage.assertUpcVisible(editedUpc);
        materialUpcPage.assertUpcNotVisible(upc); // ✅ опціонально, але дуже корисно
    }

    private static int generateUpc() {
        // 9-значний UPC (без leading zero проблем)
        return 100_000_000 + new Random().nextInt(900_000_000);
    }

    @Test
    @Order(2)
    @DisplayName("Delete UPC from Material")
    void deleteUpcForMaterial() {

        materialUpcPage
                .clickManageUpc()
                .deleteUpc()
                .clickSave();

        materialUpcPage.assertUpcNotVisible(editedUpc); // або upc, якщо видаляєш перший
    }

}