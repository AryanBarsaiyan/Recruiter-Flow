package com.futurescope.platform.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Logs resolved config from .env / application.properties so we can verify
 * spring.config.import=optional:file:.env[.properties] is loading correctly.
 * Runs early (when this config is processed), before DataSource connects.
 */
@Configuration
public class EnvConfigLogger {

    private static final Logger log = LoggerFactory.getLogger(EnvConfigLogger.class);

    @Bean
    EnvConfigLog envConfigLog(Environment env) {
        String rawDbUrl = env.getProperty("DB_URL");
        String rawDbUser = env.getProperty("DB_USERNAME");
        String resolvedUrl = env.getProperty("spring.datasource.url");
        String resolvedUser = env.getProperty("spring.datasource.username");

        if (rawDbUrl != null || rawDbUser != null) {
            log.info("[env] .env loaded; datasource url={}, username={}", maskUrl(resolvedUrl), resolvedUser);
        } else {
            log.info("[env] .env not present; datasource url={}, username={}", maskUrl(resolvedUrl), resolvedUser);
        }

        return new EnvConfigLog(rawDbUrl != null, resolvedUrl, resolvedUser);
    }

    private static String maskUrl(String url) {
        if (url == null) return "null";
        // Don't log passwords; mask query params if any
        if (url.contains("?")) {
            return url.substring(0, url.indexOf('?')) + "?***";
        }
        return url;
    }

    /** Holder for optional assertions in tests. */
    public record EnvConfigLog(boolean envFileLoaded, String datasourceUrl, String datasourceUsername) {}
}
