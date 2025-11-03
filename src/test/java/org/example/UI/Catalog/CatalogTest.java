package org.example.UI.Catalog;

import org.example.BaseUITestExtension.PlaywrightUiLoginBaseTest;
import org.example.UI.PageObjectModels.Catalog.CatalogPage;
import org.junit.jupiter.api.BeforeEach;

public class CatalogTest extends PlaywrightUiLoginBaseTest {
    CatalogPage catalogPage;

    @BeforeEach
    public void setUp() {
        openPath("/catalog");
        catalogPage = new CatalogPage(page);
    }
}
