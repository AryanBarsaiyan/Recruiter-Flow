package com.futurescope.platform.candidate.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurescope.platform.AbstractIntegrationTest;
import com.futurescope.platform.TestDataHelper;
import com.futurescope.platform.auth.domain.User;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Feature tests: GET /me (candidate profile), GET /me/applications.
 */
class MeFeatureTest extends AbstractIntegrationTest {

    @Autowired WebApplicationContext webApplicationContext;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired TestDataHelper testDataHelper;

    MockMvc mockMvc;
    String candidateToken;
    String candidateEmail;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        candidateEmail = "me-cand-" + UUID.randomUUID() + "@test.com";
        User candidateUser = testDataHelper.createCandidateUser(candidateEmail,
                webApplicationContext.getBean(org.springframework.security.crypto.password.PasswordEncoder.class).encode("candidate"));
        testDataHelper.createCandidateProfile(candidateUser, "Me Test Candidate");

        String loginBody = objectMapper.writeValueAsString(Map.of("email", candidateEmail, "password", "candidate"));
        String loginRes = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        candidateToken = objectMapper.readTree(loginRes).get("accessToken").asText();
    }

    @Test
    void getMe_asCandidate_returnsProfile() throws Exception {
        mockMvc.perform(get("/me")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(candidateEmail))
                .andExpect(jsonPath("$.fullName").value("Me Test Candidate"));
    }

    @Test
    void getMeApplications_asCandidate_returnsList() throws Exception {
        mockMvc.perform(get("/me/applications")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getSavedJobs_asCandidate_returnsList() throws Exception {
        mockMvc.perform(get("/me/saved-jobs")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getMe_asRecruiter_returns400() throws Exception {
        String recruiterEmail = "me-rec-" + UUID.randomUUID() + "@test.com";
        String signupBody = objectMapper.writeValueAsString(Map.of(
                "email", recruiterEmail,
                "password", "password1",
                "fullName", "Rec",
                "companyName", "Me Rec Co " + UUID.randomUUID().toString().substring(0, 8)));
        String signupRes = mockMvc.perform(post("/auth/signup-super-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String recruiterToken = objectMapper.readTree(signupRes).get("accessToken").asText();

        mockMvc.perform(get("/me")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Not a candidate"));
    }
}
