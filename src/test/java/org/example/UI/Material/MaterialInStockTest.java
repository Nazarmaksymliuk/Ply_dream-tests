package org.example.UI.Material;

import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import net.datafaker.Faker;
import org.assertj.core.api.Assertions;
import org.example.Api.helpers.LoginHelper.LoginClient;
import org.example.Api.helpers.LoginHelper.LoginResponse;
import org.example.Api.helpers.LocationsHelper.LocationsClient;
import org.example.BaseUITestExtension.PlaywrightUiLoginBaseTest;
import org.example.UI.Models.Material;
import org.example.UI.PageObjectModels.Alerts.AlertUtils;
import org.example.UI.PageObjectModels.Material.MaterialEditAvailabilityFlow.MaterialEditAvailabilityPopUpPage;
import org.example.UI.PageObjectModels.Material.MaterialsCreationFlow.MaterialSpecsPage;
import org.example.UI.PageObjectModels.Material.MaterialsCreationFlow.PriceAndVariantsPage;
import org.example.UI.PageObjectModels.Material.MaterialsCreationFlow.MaterialStockSetupPage;
import org.example.UI.PageObjectModels.Material.MaterialsListPage;
import org.example.UI.PageObjectModels.Stock.Warehouse.WarehousePage;
import org.example.apifactories.LocationsTestDataFactory;
import org.example.creds.Users;
import org.example.routes.Routes;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Epic("Materials")
@Feature("Material in Stock CRUD")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MaterialInStockTest extends PlaywrightUiLoginBaseTest {

    private static final Logger log = LoggerFactory.getLogger(MaterialInStockTest.class);

    WarehousePage warehousePage;
    MaterialSpecsPage materialSpecsPage;
    PriceAndVariantsPage priceAndVariantsPage;
    MaterialStockSetupPage stockSetupPage;
    MaterialsListPage materialsListPage;

    private final Faker faker = new Faker();
    private final String defaultVariation = "Single";
    private final String defaultUnitOfMeasurement = "Ea";

    private APIRequestContext apiContext;
    private String createdWarehouseId;

    @BeforeAll
    void createWarehouseViaApi() throws IOException {
        // Login via API to get auth token
        Map<String, String> defaultHeaders = Map.of(
                "Content-Type", "application/json",
                "Accept", "*/*"
        );

        APIRequestContext loginCtx = playwright.request().newContext(
                new APIRequest.NewContextOptions()
                        .setBaseURL(Routes.BASE_API_URL)
                        .setExtraHTTPHeaders(defaultHeaders)
        );

        LoginClient loginClient = new LoginClient(loginCtx);
        APIResponse loginResp = loginClient.login(Users.ADMIN.email(), Users.ADMIN.password());

        Assertions.assertThat(loginResp.status())
                .as("API login must succeed")
                .isIn(200, 201, 409);

        LoginResponse parsed = loginClient.parseLoginResponse(loginResp);
        String token = parsed.getToken();
        loginCtx.dispose();

        // Create authenticated API context
        Map<String, String> authHeaders = new HashMap<>();
        authHeaders.put("Content-Type", "application/json");
        authHeaders.put("Accept", "*/*");
        authHeaders.put("Authorization", "Bearer " + token);

        apiContext = playwright.request().newContext(
                new APIRequest.NewContextOptions()
                        .setBaseURL(Routes.BASE_API_URL)
                        .setExtraHTTPHeaders(authHeaders)
        );

        // Create warehouse location
        LocationsClient locationsClient = new LocationsClient(apiContext);
        Map<String, Object> body = LocationsTestDataFactory.buildCreateWarehouseBody("UI Test Warehouse ");
        APIResponse createResp = locationsClient.createLocation(body, false);

        log.info("CREATE WAREHOUSE status: {}", createResp.status());
        Assertions.assertThat(createResp.status())
                .as("Warehouse creation must succeed")
                .isIn(200, 201);

        createdWarehouseId = locationsClient.extractLocationId(createResp);
        Assertions.assertThat(createdWarehouseId).isNotNull();
        log.info("Created warehouse for UI test: {}", createdWarehouseId);
    }

    @AfterAll
    void cleanupWarehouse() {
        if (createdWarehouseId != null && apiContext != null) {
            try {
                LocationsClient locationsClient = new LocationsClient(apiContext);
                APIResponse r = locationsClient.deleteLocation(createdWarehouseId, null, "Cleanup after UI MaterialInStock test");
                log.info("CLEANUP DELETE WAREHOUSE status: {}", r.status());
            } catch (Exception e) {
                log.warn("CLEANUP DELETE WAREHOUSE failed: {}", e.getMessage());
            }
        }
        if (apiContext != null) {
            apiContext.dispose();
        }
    }

    @BeforeEach
    public void setUp() {
        openPath("/stock/warehouse/warehousemain/" + createdWarehouseId);
        warehousePage = new WarehousePage(page);
        materialsListPage = new MaterialsListPage(page);
        warehousePage.waitForLoaded();
    }

    Material material = new Material(
            "Material" + new Faker().number().numberBetween(10000, 99999),
            "ITEM-" + new Faker().number().numberBetween(10000, 99999),
            "Sample description",
            "BrandXXX",
            "ManufacturerYYY",
            "CategoryZZZ",
            "Ea",
            "Single",
            "Single Description",
            25.5,
            15.5,
            10
    );

    @DisplayName("Create material from Stock (WarehouseMain)")
    @Order(0)
    @Test
    public void createMaterialFromStockTest() {

        materialSpecsPage = warehousePage.clickOnAddNewMaterialButton();

        materialSpecsPage.setMaterialName(material.name);
        materialSpecsPage.setItemNumber(material.itemNumber);
        materialSpecsPage.setDescription(material.description);
        materialSpecsPage.setBrand(material.brand);
        materialSpecsPage.setManufacturer(material.manufacturer);

        materialSpecsPage.setVariantName(material.variationName);
        materialSpecsPage.setVariantDescription(material.variationDescription);

        priceAndVariantsPage = materialSpecsPage.clickNextButton();
        priceAndVariantsPage.setCostForClient(material.costForClient);
        priceAndVariantsPage.setCostForBusiness(material.costForBusiness);

        stockSetupPage = priceAndVariantsPage.clickNextButton();
        stockSetupPage.clickAddLocationButton();
        stockSetupPage.setQuantity(material.quantity);
        stockSetupPage.clickSaveLocationButton();
        stockSetupPage.clickSaveButton();

        AlertUtils.waitForAlertVisible(page);
        String alert = AlertUtils.getAlertText(page);
        Assertions.assertThat(alert).contains("successfully");
        AlertUtils.waitForAlertHidden(page);

        Assertions.assertThat(materialsListPage.getFirstMaterialNameInTheList()).isEqualTo(material.name);
        Assertions.assertThat(materialsListPage.getFirstItemNumberInTheList()).isEqualTo(material.itemNumber);
    }

    Material editedMaterial = new Material(
            "Material-edited" + new Faker().number().numberBetween(10000, 99999),
            "ITEM-edited" + new Faker().number().numberBetween(10000, 99999),
            "Sample description-edited",
            "BrandXXX-edited",
            "ManufacturerYYY-edited",
            "CategoryZZZ",
            "Ea",
            "Single",
            "Single Description-edited",
            25.5,
            15.5,
            1000
    );

    @DisplayName("Update Material in Stock (WarehouseMain)")
    @Order(1)
    @Test
    public void updateMaterialAvailabilityInStockTest(){
        int beforeQty = materialsListPage.getFirstRowQuantity();

        materialsListPage.openFirstRowMaterialStockThreeDots();
        materialsListPage.chooseMenuEditMaterialAvailability();

        MaterialEditAvailabilityPopUpPage popup = new MaterialEditAvailabilityPopUpPage(page);
        popup.waitForLoaded();

        int newQty = 100;
        int newMin = 50;
        int newMax = 150;

        popup.setQuantity(newQty);
        popup.setMinAmount(newMin);
        popup.setMaxAmount(newMax);
        popup.clickSaveChanges();

        AlertUtils.waitForAlertVisible(page);
        String alert = AlertUtils.getAlertText(page);
        Assertions.assertThat(alert).contains("successfully");
        AlertUtils.waitForAlertHidden(page);

        int afterQty = materialsListPage.getFirstRowQuantity();
        Assertions.assertThat(afterQty)
                .as("Quantity in grid should be updated")
                .isEqualTo(newQty);
    }

    @DisplayName("Delete Material in Stock (WarehouseMain)")
    @Order(2)
    @Test
    public void deleteMaterialInStockTest(){
        String firstNameForDeleting = materialsListPage.getFirstMaterialNameInTheList();

        materialsListPage.openFirstRowMaterialStockThreeDots();
        materialsListPage.chooseMenuDeleteMaterial();
        materialsListPage.confirmDeleteMaterialInModal();

        AlertUtils.waitForAlertVisible(page);
        String alert = AlertUtils.getAlertText(page);
        Assertions.assertThat(alert).contains("deleted");
        AlertUtils.waitForAlertHidden(page);

        waitForElementRemoved(firstNameForDeleting);
        Assertions.assertThat(materialsListPage.getMaterialNamesList()).doesNotContain(firstNameForDeleting);
    }
}
