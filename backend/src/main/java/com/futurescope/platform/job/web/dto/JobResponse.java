package com.futurescope.platform.job.web.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class JobResponse {

    private UUID id;
    private UUID companyId;
    private String title;
    private String description;
    private String location;
    private String employmentType;
    private boolean published;
    private OffsetDateTime applicationDeadline;
    private String customFormSchemaJson;
    private String companyName;
    private String brandingConfigJson;
    private UUID pipelineId;
    private String pipelineName;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public void setCompanyId(UUID companyId) {
        this.companyId = companyId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public OffsetDateTime getApplicationDeadline() {
        return applicationDeadline;
    }

    public void setApplicationDeadline(OffsetDateTime applicationDeadline) {
        this.applicationDeadline = applicationDeadline;
    }

    public String getCustomFormSchemaJson() {
        return customFormSchemaJson;
    }

    public void setCustomFormSchemaJson(String customFormSchemaJson) {
        this.customFormSchemaJson = customFormSchemaJson;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getBrandingConfigJson() {
        return brandingConfigJson;
    }

    public void setBrandingConfigJson(String brandingConfigJson) {
        this.brandingConfigJson = brandingConfigJson;
    }

    public UUID getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(UUID pipelineId) {
        this.pipelineId = pipelineId;
    }

    public String getPipelineName() {
        return pipelineName;
    }

    public void setPipelineName(String pipelineName) {
        this.pipelineName = pipelineName;
    }
}

