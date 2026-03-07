package com.futurescope.platform.schedule.web;

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

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Feature tests: get invitation by token, book slot (public).
 */
class SchedulingFeatureTest extends AbstractIntegrationTest {

    @Autowired WebApplicationContext webApplicationContext;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired CompanyMemberRepository companyMemberRepository;
    @Autowired JobRepository jobRepository;
    @Autowired InterviewInvitationRepository invitationRepository;
    @Autowired TestDataHelper testDataHelper;

    MockMvc mockMvc;
    String recruiterToken;
    UUID companyId;
    UUID jobId;
    UUID applicationId;
    String invitationToken;
    String candidateEmail;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        candidateEmail = "sched-candidate-" + UUID.randomUUID() + "@test.com";
        recruiterToken = signupAndGetToken();
        companyId = getCompanyIdForUser(recruiterEmailForLookup);
        createPipeline(companyId, recruiterToken);
        jobId = createJobViaApi();
        var jobEntity = jobRepository.findById(jobId).orElseThrow();
        testDataHelper.createTwoQuestionsForCompany(jobEntity.getCompany(), jobEntity);
        User candidateUser = testDataHelper.createCandidateUser(candidateEmail,
                webApplicationContext.getBean(org.springframework.security.crypto.password.PasswordEncoder.class).encode("candidate"));
        testDataHelper.createCandidateProfile(candidateUser, "Sched Candidate");
        applicationId = applyViaApi();
        invitationToken = invitationRepository.findByApplicationId(applicationId).orElseThrow().getToken();
    }

    private String signupAndGetToken() throws Exception {
        String email = "sched-" + UUID.randomUUID() + "@test.com";
        String body = objectMapper.writeValueAsString(Map.of(
                "email", email,
                "password", "password1",
                "fullName", "Rec",
                "companyName", "Sched Co " + UUID.randomUUID().toString().substring(0, 8)));
        String res = mockMvc.perform(post("/auth/signup-super-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        recruiterEmailForLookup = email;
        return objectMapper.readTree(res).get("accessToken").asText();
    }

    private volatile String recruiterEmailForLookup;

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

    private UUID getCompanyIdForUser(String email) {
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        return companyMemberRepository.findByUser(user).stream().findFirst().orElseThrow().getCompany().getId();
    }

    private UUID createJobViaApi() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("companyId", companyId.toString(), "title", "Sched Job"));
        String res = mockMvc.perform(post("/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(res).get("id").asText());
    }

    private UUID applyViaApi() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "email", candidateEmail,
                "fullName", "Sched Candidate",
                "resumeStoragePath", "/test/resume.pdf",
                "resumeOriginalFilename", "resume.pdf"));
        String res = mockMvc.perform(post("/jobs/{jobId}/apply", jobId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(res).get("id").asText());
    }

    @Test
    void getInvitationByToken_returnsInvitationInfo() throws Exception {
        mockMvc.perform(get("/interview-invitations/{token}", invitationToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobTitle").value("Sched Job"))
                .andExpect(jsonPath("$.status").value("pending"));
    }

    @Test
    void getInvitationByToken_invalidToken_returns400() throws Exception {
        mockMvc.perform(get("/interview-invitations/{token}", "invalid-token-12345"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invitation not found"));
    }

    @Test
    void bookSlot_acceptsInvitationAndReturnsSlot() throws Exception {
        OffsetDateTime start = OffsetDateTime.now().plusDays(1);
        OffsetDateTime end = start.plusHours(1);
        mockMvc.perform(post("/interview-invitations/{token}/slots", invitationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "scheduledStartAt", start.toString(),
                                "scheduledEndAt", end.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.scheduledStartAt").exists());
    }
}
