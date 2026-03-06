package com.futurescope.platform.proctoring.web;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Feature tests: proctoring start session, post event, end session (candidate auth).
 */
class ProctoringFeatureTest extends AbstractIntegrationTest {

    @Autowired WebApplicationContext webApplicationContext;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired CompanyMemberRepository companyMemberRepository;
    @Autowired JobRepository jobRepository;
    @Autowired InterviewInvitationRepository invitationRepository;
    @Autowired TestDataHelper testDataHelper;

    MockMvc mockMvc;
    String candidateToken;
    UUID interviewId;
    String recruiterEmail;
    String candidateEmail;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        recruiterEmail = "proc-" + UUID.randomUUID() + "@test.com";
        candidateEmail = "proc-cand-" + UUID.randomUUID() + "@test.com";
        String recruiterToken = signupAndGetToken();
        UUID companyId = getCompanyIdForUser(recruiterEmail);
        UUID jobId = createJobViaApi(recruiterToken, companyId);
        var jobEntity = jobRepository.findById(jobId).orElseThrow();
        testDataHelper.createTwoQuestionsForCompany(jobEntity.getCompany(), jobEntity);
        User candidateUser = testDataHelper.createCandidateUser(candidateEmail,
                webApplicationContext.getBean(org.springframework.security.crypto.password.PasswordEncoder.class).encode("candidate"));
        testDataHelper.createCandidateProfile(candidateUser, "Proc Candidate");
        UUID applicationId = applyViaApi(jobId);
        String invitationToken = invitationRepository.findByApplicationId(applicationId).orElseThrow().getToken();
        bookSlot(invitationToken);
        interviewId = startInterviewAndGetId(invitationToken);
        candidateToken = loginAndGetToken(candidateEmail, "candidate");
    }

    private String signupAndGetToken() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "email", recruiterEmail,
                "password", "password1",
                "fullName", "Rec",
                "companyName", "Proc Co " + UUID.randomUUID().toString().substring(0, 8)));
        String res = mockMvc.perform(post("/auth/signup-super-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(res).get("accessToken").asText();
    }

    private UUID getCompanyIdForUser(String email) {
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        return companyMemberRepository.findByUser(user).stream().findFirst().orElseThrow().getCompany().getId();
    }

    private UUID createJobViaApi(String token, UUID companyId) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("companyId", companyId.toString(), "title", "Proc Job"));
        String res = mockMvc.perform(post("/jobs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(res).get("id").asText());
    }

    private UUID applyViaApi(UUID jobId) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "email", candidateEmail,
                "fullName", "Proc Candidate",
                "resumeStoragePath", "/test/resume.pdf",
                "resumeOriginalFilename", "resume.pdf"));
        String res = mockMvc.perform(post("/jobs/{jobId}/apply", jobId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(res).get("id").asText());
    }

    private void bookSlot(String invitationToken) throws Exception {
        OffsetDateTime start = OffsetDateTime.now().plusDays(1);
        OffsetDateTime end = start.plusHours(1);
        mockMvc.perform(post("/interview-invitations/{token}/slots", invitationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "scheduledStartAt", start.toString(),
                                "scheduledEndAt", end.toString()))))
                .andExpect(status().isOk());
    }

    private UUID startInterviewAndGetId(String invitationToken) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("invitationToken", invitationToken));
        String res = mockMvc.perform(post("/interviews/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(res).get("interviewId").asText());
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("email", email, "password", password));
        String res = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(res).get("accessToken").asText();
    }

    @Test
    void startSession_withoutAuth_returns403() throws Exception {
        mockMvc.perform(post("/proctoring/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("interviewId", interviewId.toString()))))
                .andExpect(status().isForbidden());
    }

    @Test
    void startSession_postEvent_endSession_returnsRiskScore() throws Exception {
        String sessionRes = mockMvc.perform(post("/proctoring/sessions")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("interviewId", interviewId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        UUID sessionId = UUID.fromString(objectMapper.readTree(sessionRes).get("id").asText());

        mockMvc.perform(post("/proctoring/sessions/{sessionId}/events", sessionId)
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("eventType", "tab_switch", "weight", 1.0))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/proctoring/sessions/{sessionId}/end", sessionId)
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overallRiskScore").exists());
    }
}
