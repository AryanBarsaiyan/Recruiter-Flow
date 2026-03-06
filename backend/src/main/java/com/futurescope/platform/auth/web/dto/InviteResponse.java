package com.futurescope.platform.auth.web.dto;

import java.time.OffsetDateTime;

public class InviteResponse {

    private String inviteToken;
    private OffsetDateTime expiresAt;

    public InviteResponse(String inviteToken, OffsetDateTime expiresAt) {
        this.inviteToken = inviteToken;
        this.expiresAt = expiresAt;
    }

    public String getInviteToken() { return inviteToken; }
    public OffsetDateTime getExpiresAt() { return expiresAt; }
}
