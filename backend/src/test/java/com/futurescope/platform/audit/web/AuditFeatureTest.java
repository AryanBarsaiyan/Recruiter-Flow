package com.futurescope.platform.audit.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurescope.platform.AbstractIntegrationTest;
import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.auth.repository.CompanyMemberRepository;
import com.futurescope.platform.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Feature tests: list audit logs (recruiter, company-scoped).
 */
class AuditFeatureTest extends AbstractIntegrationTest {

    @Autowired WebApplicationContext webApplicationContext;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired CompanyMemberRepository companyMemberRepository;

    MockMvc mockMvc;
    String recruiterToken;
    UUID companyId;

    String recruiterEmail;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        recruiterEmail = "audit-" + UUID.randomUUID() + "@test.com";
        String body = objectMapper.writeValueAsString(Map.of(
                "email", recruiterEmail,
                "password", "password1",
                "fullName", "Rec",
                "companyName", "Audit Co " + UUID.randomUUID().toString().substring(0, 8)));
        String signupRes = mockMvc.perform(post("/auth/signup-super-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        recruiterToken = objectMapper.readTree(signupRes).get("accessToken").asText();
        User user = userRepository.findByEmailIgnoreCase(recruiterEmail).orElseThrow();
        companyId = companyMemberRepository.findByUser(user).stream().findFirst().orElseThrow().getCompany().getId();
        createPipeline(companyId, recruiterToken);
    }

    private void createPipeline(UUID companyId, String token) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "companyId", companyId.toString(),
                "name", "Default Hiring",
                "isDefault", true));
        mockMvc.perform(post("/companies/{companyId}/pipelines", companyId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    void listAuditLogs_asRecruiter_returnsPage() throws Exception {
        mockMvc.perform(get("/audit-logs")
                        .param("companyId", companyId.toString())
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void auditEntry_createdWhenJobCreated_criticalPath() throws Exception {
        mockMvc.perform(post("/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "companyId", companyId.toString(),
                                "title", "Audit Test Job"))))
                .andExpect(status().isOk());
        String body = mockMvc.perform(get("/audit-logs")
                        .param("companyId", companyId.toString())
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(body.contains("job_created"), "Audit log should contain job_created entry for critical path");
    }
}
