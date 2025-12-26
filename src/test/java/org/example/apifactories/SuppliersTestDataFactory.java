package org.example.apifactories;

import java.util.*;

public class SuppliersTestDataFactory {

    private SuppliersTestDataFactory() {
        // utility class
    }

    /**
     * POST /suppliers (або ваш endpoint createSupplier) очікує обгортку:
     * { "supplierRequestDtos": [ { ...supplier fields... } ] }
     */
    public static Map<String, Object> buildCreateSupplierRequest(String businessNamePrefix) {
        long ts = System.currentTimeMillis();
        Random random = new Random();

        Map<String, Object> supplierDto = new HashMap<>();
        supplierDto.put("address", "742 Evergreen Terrace");
        supplierDto.put("businessEmail", "contact+" + ts + "@northstar-transport.com");
        supplierDto.put("businessName", businessNamePrefix + ts);
        supplierDto.put("city", "Denver");
        supplierDto.put("contactName", "Olivia Carter " + random.nextInt(1000));
        supplierDto.put("finishOnboarding", false);
        supplierDto.put("hphSupplier", false);
        supplierDto.put("note", "Priority partner for refrigerated shipments. Created via API E2E test " + ts);
        supplierDto.put("paymentAccountId", "acc_" + ts);
        supplierDto.put("paymentOnboarded", false);
        supplierDto.put("phoneNumber", "+1415555" + (1000 + random.nextInt(8999)));
        supplierDto.put("profileImage", "https://example.com/logo.png");
        supplierDto.put("sendInvitationEmails", false);
        supplierDto.put("state", "COLORADO");
        supplierDto.put("tags", List.of("refrigerated", "priority", "api-test"));
        supplierDto.put("usZipCode", "80202");
        supplierDto.put("website", "https://www.northstar-transport.com");

        Map<String, Object> body = new HashMap<>();
        body.put("supplierRequestDtos", List.of(supplierDto));

        return body;
    }

    /**
     * PUT /suppliers/{id}
     */
    public static Map<String, Object> buildUpdateSupplierRequest(String supplierId, String businessNamePrefix) {
        Map<String, Object> body = new HashMap<>();
        long ts = System.currentTimeMillis();

        // якщо бекенд очікує id у body — залишаємо, якщо ні — не завадить
        body.put("id", supplierId);

        body.put("businessName", businessNamePrefix + ts);
        body.put("businessEmail", "api-crud-supplier-upd-" + ts + "@example.com");
        body.put("contactName", "API CRUD Contact UPDATED " + ts);

        body.put("phoneNumber", "+14155550199");
        body.put("city", "Lviv");
        body.put("note", "Updated via Supplier CRUD API test at " + ts);

        body.put("address", "Updated street 2");
        body.put("state", "COLORADO");
        body.put("usZipCode", "54321");
        body.put("website", "https://www.updated-supplier.com");
        body.put("profileImage", "https://example.com/logo-updated.png");
        body.put("paymentAccountId", "acc_upd_" + ts);

        body.put("finishOnboarding", false);
        body.put("paymentOnboarded", false);
        body.put("sendInvitationEmails", false);
        body.put("sampleData", false);
        body.put("hphSupplier", false);
        body.put("editable", true);
        body.put("verified", false);

        body.put("supplierAdditionalContactInfos", List.of());

        return body;
    }
}
