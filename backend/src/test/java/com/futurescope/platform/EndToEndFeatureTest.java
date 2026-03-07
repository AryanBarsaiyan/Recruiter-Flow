package com.futurescope.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.auth.repository.CompanyMemberRepository;
import com.futurescope.platform.auth.repository.UserRepository;
import com.futurescope.platform.interview.domain.Interview;
import com.futurescope.platform.interview.repository.InterviewRepository;
import com.futurescope.platform.job.repository.JobRepository;
import com.futurescope.platform.schedule.repository.InterviewInvitationRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full end-to-end feature test: signup → job → apply → invitation → book slot → start interview
 * → proctoring (as candidate) → report (as recruiter) → audit logs.
 */
@Order(1)
class EndToEndFeatureTest extends AbstractIntegrationTest {

    @Autowired WebApplicationContext webApplicationContext;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired CompanyMemberRepository companyMemberRepository;
    @Autowired InterviewInvitationRepository invitationRepository;
    @Autowired InterviewRepository interviewRepository;
    @Autowired JobRepository jobRepository;
    @Autowired TestDataHelper testDataHelper;

    MockMvc mockMvc;
    String recruiterToken;
    String candidateToken;
    UUID companyId;
    UUID jobId;
    UUID applicationId;
    String invitationToken;
    UUID interviewId;

    String recruiterEmail;
    String candidateEmail;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        recruiterEmail = "e2e-rec-" + UUID.randomUUID() + "@test.com";
        candidateEmail = "e2e-cand-" + UUID.randomUUID() + "@test.com";
    }

    @Test
    void fullFlow_signupCreateJobApplyScheduleInterviewProctoringReportAudit() throws Exception {
        // 1. Signup super admin (recruiter)
        recruiterToken = signupAndGetAccessToken();
        companyId = getCompanyIdForCurrentUser(recruiterEmail);

        // 2. Create pipeline, then job (recruiter)
        createPipelineForCompany(companyId, recruiterToken);
        jobId = createJobAndGetId();
        // 3. Create 2 questions for interview start (need company + job entities)
        var jobEntity = jobRepository.findById(jobId).orElseThrow();
        testDataHelper.createTwoQuestionsForCompany(jobEntity.getCompany(), jobEntity);

        // 4. Create candidate user + profile (known password so we can login later)
        User candidateUser = testDataHelper.createCandidateUser(candidateEmail, getPasswordEncoder().encode("candidate123"));
        testDataHelper.createCandidateProfile(candidateUser, "E2E Candidate");

        // 5. Apply to job (uses candidate email -> links to existing candidate)
        applicationId = applyAndGetApplicationId();

        // 6. Get invitation token for this application
        invitationToken = invitationRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new AssertionError("Invitation should exist after shortlist"))
                .getToken();

        // 7. Get invitation by token (public)
        mockMvc.perform(get("/interview-invitations/{token}", invitationToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobTitle").exists())
                .andExpect(jsonPath("$.status").value("pending"));

        // 8. Book slot (public)
        OffsetDateTime start = OffsetDateTime.now().plusDays(1);
        OffsetDateTime end = start.plusHours(1);
        mockMvc.perform(post("/interview-invitations/{token}/slots", invitationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "scheduledStartAt", start.toString(),
                                "scheduledEndAt", end.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty());

        // 9. Start interview (public, with invitation token)
        String startBody = objectMapper.writeValueAsString(Map.of("invitationToken", invitationToken));
        String startResponse = mockMvc.perform(post("/interviews/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(startBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.interviewId").isNotEmpty())
                .andExpect(jsonPath("$.firstQuestionId").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        interviewId = UUID.fromString(objectMapper.readTree(startResponse).get("interviewId").asText());

        // 10. Login as candidate
        candidateToken = loginAndGetAccessToken(candidateEmail, "candidate123");

        // 11. Proctoring: start session, post event, end (candidate auth)
        String sessionResponse = mockMvc.perform(post("/proctoring/sessions")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("interviewId", interviewId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        UUID sessionId = UUID.fromString(objectMapper.readTree(sessionResponse).get("id").asText());

        mockMvc.perform(post("/proctoring/sessions/{sessionId}/events", sessionId)
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "eventType", "tab_switch",
                                "weight", 1.0))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/proctoring/sessions/{sessionId}/end", sessionId)
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overallRiskScore").exists());

        // 12. Create report for interview (so recruiter can fetch it)
        Interview interview = interviewRepository.findById(interviewId).orElseThrow();
        testDataHelper.createReportForInterview(interview);

        // 13. Recruiter fetches report
        mockMvc.perform(get("/reports/applications/{applicationId}", applicationId)
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overallScore").exists())
                .andExpect(jsonPath("$.factors").isArray());

        // 14. Recruiter fetches audit logs
        mockMvc.perform(get("/audit-logs")
                        .param("companyId", companyId.toString())
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk());
    }

    private org.springframework.security.crypto.password.PasswordEncoder getPasswordEncoder() {
        return webApplicationContext.getBean(org.springframework.security.crypto.password.PasswordEncoder.class);
    }

    private String signupAndGetAccessToken() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "email", recruiterEmail,
                "password", "recruiter123",
                "fullName", "E2E Recruiter",
                "companyName", "E2E Company " + UUID.randomUUID().toString().substring(0, 8)));
        String response = mockMvc.perform(post("/auth/signup-super-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }

    private void createPipelineForCompany(UUID companyId, String token) throws Exception {
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

    private UUID getCompanyIdForCurrentUser(String email) {
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        return companyMemberRepository.findByUser(user).stream()
                .findFirst()
                .orElseThrow()
                .getCompany()
                .getId();
    }

    private UUID createJobAndGetId() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "companyId", companyId.toString(),
                "title", "E2E Backend Role"));
        String response = mockMvc.perform(post("/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(response).get("id").asText());
    }

    private UUID applyAndGetApplicationId() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "email", candidateEmail,
                "fullName", "E2E Candidate",
                "resumeStoragePath", "/test/resume.pdf",
                "resumeOriginalFilename", "resume.pdf"));
        String response = mockMvc.perform(post("/jobs/{jobId}/apply", jobId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("invited"))
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(response).get("id").asText());
    }

    private String loginAndGetAccessToken(String email, String password) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("email", email, "password", password));
        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }
}
