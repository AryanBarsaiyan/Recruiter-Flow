package com.futurescope.platform.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    /** Base64-encoded secret for signing JWTs. */
    private String secret = "ZmFrZV9mYWtlX2Zha2VfZmFrZV9mYWtlX2Zha2VfZmFrZV9mYWtlX2Zha2U=";

    /** Access token validity in minutes. */
    private long accessTokenMinutes = 60;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getAccessTokenMinutes() {
        return accessTokenMinutes;
    }

    public void setAccessTokenMinutes(long accessTokenMinutes) {
        this.accessTokenMinutes = accessTokenMinutes;
    }
}
