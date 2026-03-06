package com.futurescope.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for security (401, RBAC) and request validation (400).
 */
class SecurityAndValidationIntegrationTest extends AbstractIntegrationTest {

    @Autowired WebApplicationContext webApplicationContext;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired CompanyMemberRepository companyMemberRepository;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
    }

    @Test
    void createJob_withoutAuth_returns403() throws Exception {
        mockMvc.perform(post("/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "companyId", UUID.randomUUID().toString(),
                                "title", "Job"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void createJob_withInvalidToken_returns403() throws Exception {
        mockMvc.perform(post("/jobs")
                        .header("Authorization", "Bearer invalid.jwt.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "companyId", UUID.randomUUID().toString(),
                                "title", "Job"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void getReports_withoutAuth_returns403() throws Exception {
        mockMvc.perform(get("/reports/applications/{applicationId}", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    void signup_withShortPassword_returns400() throws Exception {
        mockMvc.perform(post("/auth/signup-super-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "short@test.com",
                                "password", "short",
                                "fullName", "User",
                                "companyName", "Co"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void apply_withInvalidJobId_returns400() throws Exception {
        mockMvc.perform(post("/jobs/{jobId}/apply", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "apply@test.com",
                                "fullName", "Apply Test",
                                "resumeStoragePath", "/path/resume.pdf",
                                "resumeOriginalFilename", "resume.pdf"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Job not found"));
    }

    @Test
    void createJob_forCompanyUserNotMemberOf_returns400() throws Exception {
        String email1 = "rbac1-" + UUID.randomUUID() + "@test.com";
        String email2 = "rbac2-" + UUID.randomUUID() + "@test.com";
        String token1 = signupAndGetToken(email1, "Company One");
        String token2 = signupAndGetToken(email2, "Company Two");
        UUID company2Id = getCompanyIdForUser(email2);

        mockMvc.perform(post("/jobs")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "companyId", company2Id.toString(),
                                "title", "Job"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Not a member of this company"));
    }

    private String signupAndGetToken(String email, String companyName) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "email", email,
                "password", "password1",
                "fullName", "Rec",
                "companyName", companyName + " " + UUID.randomUUID().toString().substring(0, 8)));
        String res = mockMvc.perform(post("/auth/signup-super-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(res).get("accessToken").asText();
    }

    private UUID getCompanyIdForUser(String email) {
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        return companyMemberRepository.findByUser(user).stream()
                .findFirst().orElseThrow().getCompany().getId();
    }
}
