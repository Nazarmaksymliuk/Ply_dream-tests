package org.example.UI.PageObjectModels.Tools;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.List;

public class ToolsListPage {
    private final Page page;
    private final Locator firstToolNameInTheList;
    private final Locator firstUnitNameInTheList;
    private final Locator firstToolMFGInTheList;
    private final Locator firstToolWarehouseLocationInTheList;
    private final Locator firstToolStatusLocationInTheList;
    private final Locator firstToolDateInTheList;
    private final Locator toolNamesList;

    public ToolsListPage(Page page) {
        this.page = page;
        firstToolNameInTheList = page.locator("a.link_black[href^='/tool']").first();
        toolNamesList = page.locator("a.link_black[href^='/tool']");
        firstUnitNameInTheList = page.locator(".status.variation_name").first();
        firstToolMFGInTheList = page.locator(".fw_400.fs_12").first();
        firstToolWarehouseLocationInTheList = page.locator(".fw_500").first();
        firstToolStatusLocationInTheList = page.locator("[class*='status_']").first();
        firstToolDateInTheList = page.locator("[class^='_date_wrapper_']").first();
    }

    public String getFirstToolNameInTheList() {
        return firstToolNameInTheList.innerText();
    }
    public String getFirstUnitNameInTheList() {
        return firstUnitNameInTheList.innerText();
    }
    public String getFirstToolMFGInTheList() {
        return firstToolMFGInTheList.innerText();
    }
    public String getFirstToolUnitWarehouseLocationInTheList() {
        return firstToolWarehouseLocationInTheList.innerText();
    }
    public String getFirstToolStatusLocationInTheList() {
        return firstToolStatusLocationInTheList.innerText();
    }
    public String getFirstToolDateInTheList() {
        return firstToolDateInTheList.innerText();
    }

    public List<String> getToolNamesList() {
     return toolNamesList.allInnerTexts();
    }




}
