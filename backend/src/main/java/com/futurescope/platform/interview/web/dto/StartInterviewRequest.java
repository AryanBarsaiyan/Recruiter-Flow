package com.futurescope.platform.interview.web.dto;

import jakarta.validation.constraints.NotBlank;

public class StartInterviewRequest {

    @NotBlank
    private String invitationToken;

    public String getInvitationToken() {
        return invitationToken;
    }

    public void setInvitationToken(String invitationToken) {
        this.invitationToken = invitationToken;
    }
}
