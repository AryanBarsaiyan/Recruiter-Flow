package com.futurescope.platform.application.web.dto;

/**
 * Response after uploading a resume for the apply flow.
 * Backend stores the file and optionally extracts text to pre-fill the form.
 */
public class UploadResumeResponse {

    private String storagePath;
    private String originalFilename;
    private String extractedFullName;
    private String extractedEmail;
    private String extractedPhone;

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getExtractedFullName() {
        return extractedFullName;
    }

    public void setExtractedFullName(String extractedFullName) {
        this.extractedFullName = extractedFullName;
    }

    public String getExtractedEmail() {
        return extractedEmail;
    }

    public void setExtractedEmail(String extractedEmail) {
        this.extractedEmail = extractedEmail;
    }

    public String getExtractedPhone() {
        return extractedPhone;
    }

    public void setExtractedPhone(String extractedPhone) {
        this.extractedPhone = extractedPhone;
    }
}
