package com.futurescope.platform.job.domain;

import com.futurescope.platform.auth.domain.Company;
import com.futurescope.platform.auth.domain.User;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "jobs")
public class Job {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 255)
    private String title;

    @Column
    private String description;

    @Column(length = 255)
    private String location;

    @Column(name = "employment_type", length = 64)
    private String employmentType;

    @Column(name = "is_published", nullable = false)
    private boolean published;

    @Column(name = "application_deadline")
    private OffsetDateTime applicationDeadline;

    @Column(name = "max_applications")
    private Integer maxApplications;

    @Column(name = "resume_criteria")
    private String resumeCriteriaJson;

    @Column(name = "custom_form_schema")
    private String customFormSchemaJson;

    @Column(name = "scoring_weights_override")
    private String scoringWeightsOverrideJson;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pipeline_id")
    private Pipeline pipeline;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
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

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public Pipeline getPipeline() {
        return pipeline;
    }

    public void setPipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

