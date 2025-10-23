package org.example.UI.Catalog;

import org.example.BaseTestExtension.PlaywrightBaseTest;
import org.example.PageObjectModels.Catalog.CatalogPage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CatalogTest extends PlaywrightBaseTest {
    CatalogPage catalogPage;

    @BeforeEach
    public void setUp() {
        openPath("/catalog");
        catalogPage = new CatalogPage(page);
    }
}
