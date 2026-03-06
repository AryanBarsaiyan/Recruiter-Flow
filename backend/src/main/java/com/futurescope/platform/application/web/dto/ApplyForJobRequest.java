package com.futurescope.platform.application.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public class ApplyForJobRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String fullName;

    private String phone;

    @NotBlank
    private String resumeStoragePath;

    @NotBlank
    private String resumeOriginalFilename;

    private Map<String, Object> answers;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getResumeStoragePath() {
        return resumeStoragePath;
    }

    public void setResumeStoragePath(String resumeStoragePath) {
        this.resumeStoragePath = resumeStoragePath;
    }

    public String getResumeOriginalFilename() {
        return resumeOriginalFilename;
    }

    public void setResumeOriginalFilename(String resumeOriginalFilename) {
        this.resumeOriginalFilename = resumeOriginalFilename;
    }

    public Map<String, Object> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<String, Object> answers) {
        this.answers = answers;
    }
}

