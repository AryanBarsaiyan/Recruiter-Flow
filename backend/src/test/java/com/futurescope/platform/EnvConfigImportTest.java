package com.futurescope.platform;

import com.futurescope.platform.config.EnvConfigLogger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that spring.config.import loads optional property files (.env and env-test.properties).
 * env-test.properties is loaded in test profile via application-test.yml.
 */
class EnvConfigImportTest extends AbstractIntegrationTest {

    @Autowired
    private Environment env;

    @Autowired(required = false)
    private EnvConfigLogger.EnvConfigLog envConfigLog;

    @Test
    void configImportLoadsEnvTestProperties() {
        // env-test.properties is loaded by spring.config.import in application-test.yml
        assertThat(env.getProperty("ENV_LOADED")).isEqualTo("yes");
        assertThat(env.getProperty("TEST_PROP")).isEqualTo("from-env-test");
    }

    @Test
    void resolvedDatasourceConfigIsAvailable() {
        // Resolved props from application.properties (or .env) are present
        String url = env.getProperty("spring.datasource.url");
        String username = env.getProperty("spring.datasource.username");
        assertThat(url).isNotBlank();
        assertThat(username).isNotBlank();
    }

    @Test
    void envConfigLogBeanRecordsResolvedDatasource() {
        assertThat(envConfigLog).isNotNull();
        assertThat(envConfigLog.datasourceUrl()).isNotBlank();
        assertThat(envConfigLog.datasourceUsername()).isNotBlank();
    }
}
