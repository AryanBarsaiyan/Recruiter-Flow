package com.futurescope.platform.auth.web.dto;

import jakarta.validation.constraints.NotBlank;

public class AcceptInviteRequest {

    @NotBlank
    private String token;

    private String fullName;

    private String password;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
