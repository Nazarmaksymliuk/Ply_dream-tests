package org.example.UI.MaterialVariation;

import org.assertj.core.api.Assertions;
import org.example.BaseUITestExtension.PlaywrightUiLoginBaseTest;
import org.example.UI.PageObjectModels.Catalog.CatalogPage;
import org.example.UI.PageObjectModels.Material.MaterialPage;
import org.example.UI.PageObjectModels.Material.MaterialsCreationFlow.MaterialSpecsPage;
import org.example.UI.PageObjectModels.Material.MaterialsCreationFlow.MaterialStockSetupPage;
import org.example.UI.PageObjectModels.Material.MaterialsCreationFlow.PriceAndVariantsPage;
import org.example.UI.PageObjectModels.Material.MaterialsListPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class MaterialVariationTest extends PlaywrightUiLoginBaseTest {
    MaterialPage materialPage;
    CatalogPage catalogPage;
    MaterialSpecsPage materialSpecsPage;
    MaterialStockSetupPage stockSetupPage;
    MaterialsListPage materialsListPage;

    private final String variationName = "variation" + + new Random().nextInt(100000);

    @BeforeEach
    public void setUp() {
        openPath("/catalog");
        catalogPage = new CatalogPage(page);
        materialPage = new MaterialPage(page);
        materialsListPage = new MaterialsListPage(page);
    }


    @Test
    @Order(1)
    @DisplayName("Material Variation change test")
    public void materialVariationChangeTest() {
        materialSpecsPage = new MaterialSpecsPage(page);

        catalogPage.waitForLoaded();

        String firstNameForEditing = materialsListPage.getFirstMaterialNameInTheList();
        String firstVariation = materialsListPage.getFirstMaterialVariation();

        catalogPage.openFirstRowMaterialThreeDots();
        materialSpecsPage = catalogPage.chooseMenuEditMaterial();

        waitForElementPresent(firstNameForEditing);

        materialSpecsPage.setVariantName(variationName);
        materialSpecsPage.clickSaveButtonInTheEditMaterialFlow();

        waitForElementRemoved(firstVariation);

        Assertions.assertThat(materialsListPage.getFirstMaterialVariation()).isEqualTo(variationName);
    }

}
