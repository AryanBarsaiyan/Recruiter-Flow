package com.futurescope.platform.candidate.web.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class SavedJobResponse {

    private UUID id;
    private UUID jobId;
    private String jobTitle;
    private OffsetDateTime savedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getJobId() { return jobId; }
    public void setJobId(UUID jobId) { this.jobId = jobId; }
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public OffsetDateTime getSavedAt() { return savedAt; }
    public void setSavedAt(OffsetDateTime savedAt) { this.savedAt = savedAt; }
}
