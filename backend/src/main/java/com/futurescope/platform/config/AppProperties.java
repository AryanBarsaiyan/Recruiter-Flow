package com.futurescope.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /** Base URL of the frontend app (e.g. https://app.example.com). */
    private String baseUrl = "https://app.example.com";

    private Cors cors = new Cors();
    private ResumeUpload resumeUpload = new ResumeUpload();
    private AvatarUpload avatarUpload = new AvatarUpload();
    private E2eSeed e2eSeed = new E2eSeed();

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Cors getCors() {
        return cors;
    }

    public void setCors(Cors cors) {
        this.cors = cors;
    }

    public ResumeUpload getResumeUpload() {
        return resumeUpload;
    }

    public void setResumeUpload(ResumeUpload resumeUpload) {
        this.resumeUpload = resumeUpload;
    }

    public AvatarUpload getAvatarUpload() {
        return avatarUpload;
    }

    public void setAvatarUpload(AvatarUpload avatarUpload) {
        this.avatarUpload = avatarUpload;
    }

    public E2eSeed getE2eSeed() {
        return e2eSeed;
    }

    public void setE2eSeed(E2eSeed e2eSeed) {
        this.e2eSeed = e2eSeed;
    }

    public static class Cors {
        private String allowedOrigins = "http://localhost:3000,http://127.0.0.1:3000";

        public String getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(String allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }
    }

    public static class ResumeUpload {
        private String dir = "./uploads/resumes";

        public String getDir() {
            return dir;
        }

        public void setDir(String dir) {
            this.dir = dir;
        }
    }

    public static class AvatarUpload {
        private String dir = "./uploads/avatars";

        public String getDir() {
            return dir;
        }

        public void setDir(String dir) {
            this.dir = dir;
        }
    }

    public static class E2eSeed {
        private boolean enabled = false;
        private String candidateEmail = "test-candidate@example.com";
        private String candidatePassword = "password123";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getCandidateEmail() {
            return candidateEmail;
        }

        public void setCandidateEmail(String candidateEmail) {
            this.candidateEmail = candidateEmail;
        }

        public String getCandidatePassword() {
            return candidatePassword;
        }

        public void setCandidatePassword(String candidatePassword) {
            this.candidatePassword = candidatePassword;
        }
    }
}
