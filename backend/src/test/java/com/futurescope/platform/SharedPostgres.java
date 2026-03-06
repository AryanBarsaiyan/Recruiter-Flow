package com.futurescope.platform;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Single Postgres container shared by all integration tests for the whole JVM run.
 * Avoids per-class containers so Spring context reuse never points at a stopped container.
 */
public final class SharedPostgres {

    private static final PostgreSQLContainer<?> CONTAINER =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
                    .withDatabaseName("future_scope_test")
                    .withUsername("test")
                    .withPassword("test");

    static {
        CONTAINER.start();
    }

    public static PostgreSQLContainer<?> get() {
        return CONTAINER;
    }

    private SharedPostgres() {}
}
