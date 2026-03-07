package com.futurescope.platform.auth.web.dto;

import java.util.UUID;

/**
 * User info returned with login, signup, and refresh so the frontend
 * can show the dashboard without an extra round-trip. For recruiters,
 * defaultCompanyId is the first active company membership.
 */
public class AuthUserDto {

    private UUID id;
    private String email;
    private String userType;
    private String fullName;
    private UUID defaultCompanyId;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public UUID getDefaultCompanyId() {
        return defaultCompanyId;
    }

    public void setDefaultCompanyId(UUID defaultCompanyId) {
        this.defaultCompanyId = defaultCompanyId;
    }
}
