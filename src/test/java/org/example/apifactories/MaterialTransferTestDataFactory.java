package org.example.apifactories;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaterialTransferTestDataFactory {

    public static Map<String, Object> buildTransferRequest(
            String toLocationId,
            String materialDetailsLocationId,
            int transferQty,
            double price,
            String reason
    ) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", materialDetailsLocationId);
        dto.put("notes", null);
        dto.put("transfer", transferQty);
        dto.put("price", price);
        dto.put("reason", reason);
        dto.put("serialNumbers", null);

        Map<String, Object> body = new HashMap<>();
        body.put("toLocationId", toLocationId);
        body.put("materialDetailsLocationRequestDtos", List.of(dto));

        return body;
    }
}
