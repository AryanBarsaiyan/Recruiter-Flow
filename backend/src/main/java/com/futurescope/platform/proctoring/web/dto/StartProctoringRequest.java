package com.futurescope.platform.proctoring.web.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class StartProctoringRequest {

    @NotNull
    private UUID interviewId;

    public UUID getInterviewId() {
        return interviewId;
    }

    public void setInterviewId(UUID interviewId) {
        this.interviewId = interviewId;
    }
}
