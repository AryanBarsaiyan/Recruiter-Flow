package com.futurescope.platform.job.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

public class CreateJobRequest {

    @NotNull
    private UUID companyId;

    @NotBlank
    private String title;

    private String description;

    private String location;

    private String employmentType;

    private OffsetDateTime applicationDeadline;

    private Integer maxApplications;

    private String resumeCriteriaJson;

    private String customFormSchemaJson;

    private String scoringWeightsOverrideJson;

    private UUID pipelineId;

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

    public OffsetDateTime getApplicationDeadline() {
        return applicationDeadline;
    }

    public void setApplicationDeadline(OffsetDateTime applicationDeadline) {
        this.applicationDeadline = applicationDeadline;
    }

    public Integer getMaxApplications() {
        return maxApplications;
    }

    public void setMaxApplications(Integer maxApplications) {
        this.maxApplications = maxApplications;
    }

    public String getResumeCriteriaJson() {
        return resumeCriteriaJson;
    }

    public void setResumeCriteriaJson(String resumeCriteriaJson) {
        this.resumeCriteriaJson = resumeCriteriaJson;
    }

    public String getCustomFormSchemaJson() {
        return customFormSchemaJson;
    }

    public void setCustomFormSchemaJson(String customFormSchemaJson) {
        this.customFormSchemaJson = customFormSchemaJson;
    }

    public String getScoringWeightsOverrideJson() {
        return scoringWeightsOverrideJson;
    }

    public void setScoringWeightsOverrideJson(String scoringWeightsOverrideJson) {
        this.scoringWeightsOverrideJson = scoringWeightsOverrideJson;
    }

    public UUID getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(UUID pipelineId) {
        this.pipelineId = pipelineId;
    }
}

