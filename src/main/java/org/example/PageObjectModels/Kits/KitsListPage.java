package org.example.PageObjectModels.Kits;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.List;

public class KitsListPage {
    private final Page page;

    // Колонки першого рядка
    private final Locator firstKitNameInTheList;
    private final Locator firstKitQtyMaterialsInTheList;
    private final Locator firstKitCostInTheList;      // стовпчик $ COST
    private final Locator firstKitBusinessInTheList;  // стовпчик $ BUSINESS

    // Усі назви кітів (лінки) у списку
    private final Locator kitNamesList;

    public KitsListPage(Page page) {
        this.page = page;

        // Назва кіта — посилання у колонці NAME
        this.firstKitNameInTheList = page.locator("[href^='/kit']").first();

        // Усі назви кітів (для списку)
        this.kitNamesList = page.locator("[href^='/kit']");

        // Qty Materials — друга текстова клітинка після NAME (зазвичай індекс 2 з урахуванням чекбокса)
        this.firstKitQtyMaterialsInTheList = page.locator("td").nth(2);

        // Вартість — перший елемент у рядку, що починається з $
        this.firstKitBusinessInTheList = page.locator("").first();

        // Business — друге грошове значення у рядку
        this.firstKitCostInTheList = page.locator(".font-semibold").nth(2);
    }

    // ====== Getters (текст) ======
    public String getFirstKitNameInTheList() {
        return firstKitNameInTheList.innerText();
    }

    public String getFirstKitQtyMaterialsInTheList() {
        return firstKitQtyMaterialsInTheList.innerText();
    }

    public String getFirstKitCostTextInTheList() {
        return firstKitCostInTheList.innerText();      // напр., "$25.50"
    }

    public String getFirstKitBusinessTextInTheList() {
        return firstKitBusinessInTheList.innerText();  // напр., "$15.50"
    }

    public List<String> getKitNamesList() {
        return kitNamesList.allInnerTexts();
    }

    // ====== Getters (Double) ======
    public Double getFirstKitCostInTheListAsDouble() {
        return parseMoney(getFirstKitCostTextInTheList());
    }

    public Double getFirstKitBusinessInTheListAsDouble() {
        return parseMoney(getFirstKitBusinessTextInTheList());
    }

    // ====== Helpers ======
    private Double parseMoney(String moneyText) {
        // "$25.50" -> "25.50" -> 25.5
        String normalized = moneyText.replaceAll("[^0-9.,]", "")
                .replace(",", ".");
        if (normalized.isEmpty()) return 0.0;
        return Double.parseDouble(normalized);
    }
}
