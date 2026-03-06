package com.futurescope.platform.ai.screening.client.dto;

public class ResumeScreeningRequest {

    private String resumeText;
    private String resumeMetadataJson;
    private String criteriaJson;

    public String getResumeText() {
        return resumeText;
    }

    public void setResumeText(String resumeText) {
        this.resumeText = resumeText;
    }

    public String getResumeMetadataJson() {
        return resumeMetadataJson;
    }

    public void setResumeMetadataJson(String resumeMetadataJson) {
        this.resumeMetadataJson = resumeMetadataJson;
    }

    public String getCriteriaJson() {
        return criteriaJson;
    }

    public void setCriteriaJson(String criteriaJson) {
        this.criteriaJson = criteriaJson;
    }
}

