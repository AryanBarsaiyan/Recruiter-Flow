package com.futurescope.platform.auth.web.dto;

import java.util.UUID;

public class CompanyResponse {

    private UUID id;
    private String name;
    private String slug;
    private String brandingConfigJson;
    private boolean active;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getBrandingConfigJson() {
        return brandingConfigJson;
    }

    public void setBrandingConfigJson(String brandingConfigJson) {
        this.brandingConfigJson = brandingConfigJson;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
