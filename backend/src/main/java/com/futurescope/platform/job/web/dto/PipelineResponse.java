package com.futurescope.platform.job.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class PipelineResponse {

    private UUID id;
    private UUID companyId;
    private String name;

    @JsonProperty("default")
    private boolean isDefault;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }
}
