package com.futurescope.platform.application.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurescope.platform.AbstractIntegrationTest;
import com.futurescope.platform.TestDataHelper;
import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.auth.repository.CompanyMemberRepository;
import com.futurescope.platform.auth.repository.UserRepository;
import com.futurescope.platform.job.repository.JobRepository;
import com.futurescope.platform.schedule.repository.InterviewInvitationRepository;
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
 * Feature tests: GET /applications/{id} as recruiter and as candidate.
 */
class ApplicationsFeatureTest extends AbstractIntegrationTest {

    @Autowired WebApplicationContext webApplicationContext;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired CompanyMemberRepository companyMemberRepository;
    @Autowired JobRepository jobRepository;
    @Autowired InterviewInvitationRepository invitationRepository;
    @Autowired TestDataHelper testDataHelper;

    MockMvc mockMvc;
    String recruiterToken;
    String candidateToken;
    UUID companyId;
    UUID jobId;
    UUID applicationId;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        String recruiterEmail = "app-rec-" + UUID.randomUUID() + "@test.com";
        String candidateEmail = "app-cand-" + UUID.randomUUID() + "@test.com";

        String signupBody = objectMapper.writeValueAsString(Map.of(
                "email", recruiterEmail,
                "password", "password1",
                "fullName", "Rec",
                "companyName", "App Co " + UUID.randomUUID().toString().substring(0, 8)));
        String signupRes = mockMvc.perform(post("/auth/signup-super-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        recruiterToken = objectMapper.readTree(signupRes).get("accessToken").asText();
        User recruiter = userRepository.findByEmailIgnoreCase(recruiterEmail).orElseThrow();
        companyId = companyMemberRepository.findByUser(recruiter).stream().findFirst().orElseThrow().getCompany().getId();

        String jobReq = objectMapper.writeValueAsString(Map.of("companyId", companyId.toString(), "title", "App Test Job"));
        String jobRes = mockMvc.perform(post("/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jobReq))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        jobId = UUID.fromString(objectMapper.readTree(jobRes).get("id").asText());

        var jobEntity = jobRepository.findById(jobId).orElseThrow();
        testDataHelper.createTwoQuestionsForCompany(jobEntity.getCompany(), jobEntity);
        User candidateUser = testDataHelper.createCandidateUser(candidateEmail,
                webApplicationContext.getBean(org.springframework.security.crypto.password.PasswordEncoder.class).encode("candidate"));
        testDataHelper.createCandidateProfile(candidateUser, "App Candidate");

        String applyBody = objectMapper.writeValueAsString(Map.of(
                "email", candidateEmail,
                "fullName", "App Candidate",
                "resumeStoragePath", "/test/resume.pdf",
                "resumeOriginalFilename", "resume.pdf"));
        String applyRes = mockMvc.perform(post("/jobs/{jobId}/apply", jobId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(applyBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        applicationId = UUID.fromString(objectMapper.readTree(applyRes).get("id").asText());

        String loginBody = objectMapper.writeValueAsString(Map.of("email", candidateEmail, "password", "candidate"));
        String loginRes = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        candidateToken = objectMapper.readTree(loginRes).get("accessToken").asText();
    }

    @Test
    void getApplication_byId_asRecruiter_returnsApplication() throws Exception {
        mockMvc.perform(get("/applications/{id}", applicationId)
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(applicationId.toString()))
                .andExpect(jsonPath("$.jobId").value(jobId.toString()))
                .andExpect(jsonPath("$.status").exists());
    }

    @Test
    void getApplication_byId_asCandidate_returnsOwnApplication() throws Exception {
        mockMvc.perform(get("/applications/{id}", applicationId)
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(applicationId.toString()))
                .andExpect(jsonPath("$.jobId").value(jobId.toString()));
    }

    @Test
    void getStageProgress_asRecruiter_returnsList() throws Exception {
        mockMvc.perform(get("/applications/{id}/stage", applicationId)
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
