package com.futurescope.platform;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApplicationContextTest extends AbstractIntegrationTest {

    @Test
    void contextLoads(ApplicationContext context) {
        assertNotNull(context);
    }
}
