package org.example.Api.helpers.MaterialTransfer;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;

import java.util.Map;

public class MaterialTransferClient {
    private final APIRequestContext api;

    public MaterialTransferClient(APIRequestContext api) {
        this.api = api;
    }

    /**
     * PATCH /locations/{fromLocationId}/transfering
     */
    public APIResponse transfer(String fromLocationId, Map<String, Object> body) {
        return api.patch(
                "/locations/" + fromLocationId + "/transfering",
                RequestOptions.create().setData(body)
        );
    }
}
