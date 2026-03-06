package com.futurescope.platform.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

    /** Whether rate limiting is enabled. */
    private boolean enabled = true;

    /** Auth endpoints (login, signup, refresh, etc.): requests per window per key. */
    private int authRequestsPerMinute = 15;

    /** Interview submit-code / followup-answer: requests per minute per key. */
    private int interviewRequestsPerMinute = 30;

    /** Proctoring session create/end: requests per minute per key. */
    private int proctoringSessionRequestsPerMinute = 10;

    /** Proctoring events: requests per minute per key (higher, many events per session). */
    private int proctoringEventsPerMinute = 120;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getAuthRequestsPerMinute() {
        return authRequestsPerMinute;
    }

    public void setAuthRequestsPerMinute(int authRequestsPerMinute) {
        this.authRequestsPerMinute = authRequestsPerMinute;
    }

    public int getInterviewRequestsPerMinute() {
        return interviewRequestsPerMinute;
    }

    public void setInterviewRequestsPerMinute(int interviewRequestsPerMinute) {
        this.interviewRequestsPerMinute = interviewRequestsPerMinute;
    }

    public int getProctoringSessionRequestsPerMinute() {
        return proctoringSessionRequestsPerMinute;
    }

    public void setProctoringSessionRequestsPerMinute(int proctoringSessionRequestsPerMinute) {
        this.proctoringSessionRequestsPerMinute = proctoringSessionRequestsPerMinute;
    }

    public int getProctoringEventsPerMinute() {
        return proctoringEventsPerMinute;
    }

    public void setProctoringEventsPerMinute(int proctoringEventsPerMinute) {
        this.proctoringEventsPerMinute = proctoringEventsPerMinute;
    }
}
