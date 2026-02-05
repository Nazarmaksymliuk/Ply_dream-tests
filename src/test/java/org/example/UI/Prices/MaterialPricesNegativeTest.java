package org.example.UI.Prices;

import com.microsoft.playwright.assertions.PlaywrightAssertions;
import org.assertj.core.api.Assertions;
import org.example.BaseUIApiExtension.PlaywrightUiApiBaseTest;
import org.example.UI.PageObjectModels.Catalog.CatalogPage;
import org.example.UI.PageObjectModels.Material.MaterialsListPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public class MaterialPricesNegativeTest extends PlaywrightUiApiBaseTest {
    CatalogPage catalogPage;
    MaterialsListPage materialsListPage;

    @BeforeEach
    public void setUp() {
        openPath("/catalog");
        catalogPage = new CatalogPage(page);
        materialsListPage = new MaterialsListPage(page);
    }

    private final Double editedCostForClient = new Random().nextDouble(1000);
    private final Double editedCostForBusiness = new Random().nextDouble(1000);

    @Test
    @Order(0)
    @DisplayName("Change Material Price in Catalog")
    public void changeMaterialPriceInCatalog() {
        String firstMaterialName = materialsListPage.getFirstMaterialNameInTheList();

        materialsListPage.clickEditPricePen();
        materialsListPage.clearPriceInTheAdjustPricesModal();
        materialsListPage.clickSaveChangesButton();

        PlaywrightAssertions.assertThat(materialsListPage.getErrorMessageLocator()).isVisible();
    }

}
