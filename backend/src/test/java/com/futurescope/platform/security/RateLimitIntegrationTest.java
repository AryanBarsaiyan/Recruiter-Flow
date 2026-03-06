package com.futurescope.platform.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurescope.platform.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies rate limiting returns 429 when limit is exceeded.
 * Uses a low limit (2/min) for auth endpoints so the 3rd request is rejected.
 */
@TestPropertySource(properties = {
        "app.rate-limit.enabled=true",
        "app.rate-limit.auth-requests-per-minute=2"
})
class RateLimitIntegrationTest extends AbstractIntegrationTest {

    @Autowired WebApplicationContext webApplicationContext;
    @Autowired ObjectMapper objectMapper;

    @Test
    void authEndpoint_exceedsLimit_returns429() throws Exception {
        MockMvc mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();

        mvc.perform(post("/auth/signup-super-admin").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "rate1-" + UUID.randomUUID() + "@test.com",
                                "password", "password123",
                                "fullName", "User",
                                "companyName", "Co " + UUID.randomUUID()))))
                .andExpect(status().isOk());
        mvc.perform(post("/auth/signup-super-admin").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "rate2-" + UUID.randomUUID() + "@test.com",
                                "password", "password123",
                                "fullName", "User",
                                "companyName", "Co " + UUID.randomUUID()))))
                .andExpect(status().isOk());
        mvc.perform(post("/auth/signup-super-admin").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "rate3-" + UUID.randomUUID() + "@test.com",
                                "password", "password123",
                                "fullName", "User",
                                "companyName", "Co " + UUID.randomUUID()))))
                .andExpect(status().isTooManyRequests());
    }
}
