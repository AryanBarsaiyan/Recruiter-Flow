package com.futurescope.platform.auth.web.dto;

public class UpdateBrandingRequest {

    private String brandingConfigJson;

    public String getBrandingConfigJson() {
        return brandingConfigJson;
    }

    public void setBrandingConfigJson(String brandingConfigJson) {
        this.brandingConfigJson = brandingConfigJson;
    }
}
