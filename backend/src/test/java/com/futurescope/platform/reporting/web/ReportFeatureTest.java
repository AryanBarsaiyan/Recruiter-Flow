package com.futurescope.platform.reporting.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurescope.platform.AbstractIntegrationTest;
import com.futurescope.platform.TestDataHelper;
import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.auth.repository.CompanyMemberRepository;
import com.futurescope.platform.auth.repository.UserRepository;
import com.futurescope.platform.interview.domain.Interview;
import com.futurescope.platform.interview.repository.InterviewRepository;
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
 * Feature tests: get report by application ID (recruiter, company-scoped).
 */
class ReportFeatureTest extends AbstractIntegrationTest {

    @Autowired WebApplicationContext webApplicationContext;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired CompanyMemberRepository companyMemberRepository;
    @Autowired JobRepository jobRepository;
    @Autowired InterviewInvitationRepository invitationRepository;
    @Autowired InterviewRepository interviewRepository;
    @Autowired TestDataHelper testDataHelper;

    MockMvc mockMvc;
    String recruiterToken;
    UUID applicationId;
    UUID jobId;

    String candidateEmail;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        candidateEmail = "report-candidate-" + UUID.randomUUID() + "@test.com";
        recruiterToken = signupAndGetToken();
        UUID companyId = getCompanyIdForUser(recruiterEmailForLookup);
        jobId = createJobViaApi(companyId);
        var jobEntity = jobRepository.findById(jobId).orElseThrow();
        testDataHelper.createTwoQuestionsForCompany(jobEntity.getCompany(), jobEntity);
        User candidateUser = testDataHelper.createCandidateUser(candidateEmail,
                webApplicationContext.getBean(org.springframework.security.crypto.password.PasswordEncoder.class).encode("candidate"));
        testDataHelper.createCandidateProfile(candidateUser, "Report Candidate");
        applicationId = applyViaApi(jobId);
        String invitationToken = invitationRepository.findByApplicationId(applicationId).orElseThrow().getToken();
        bookSlot(invitationToken);
        UUID interviewId = startInterviewAndGetId(invitationToken);
        Interview interview = interviewRepository.findById(interviewId).orElseThrow();
        testDataHelper.createReportForInterview(interview);
    }

    private volatile String recruiterEmailForLookup;

    private String signupAndGetToken() throws Exception {
        recruiterEmailForLookup = "report-" + UUID.randomUUID() + "@test.com";
        String body = objectMapper.writeValueAsString(Map.of(
                "email", recruiterEmailForLookup,
                "password", "password1",
                "fullName", "Rec",
                "companyName", "Report Co " + UUID.randomUUID().toString().substring(0, 8)));
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

    private UUID createJobViaApi(UUID companyId) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("companyId", companyId.toString(), "title", "Report Job"));
        String res = mockMvc.perform(post("/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(res).get("id").asText());
    }

    private UUID applyViaApi(UUID jobId) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "email", candidateEmail,
                "fullName", "Report Candidate",
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

    @Test
    void getReportByApplicationId_asRecruiter_returnsReport() throws Exception {
        mockMvc.perform(get("/reports/applications/{applicationId}", applicationId)
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overallScore").exists())
                .andExpect(jsonPath("$.factors").isArray());
    }

    @Test
    void getSummaryByJob_returnsSummary() throws Exception {
        mockMvc.perform(get("/reports/jobs/{jobId}", jobId)
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value(jobId.toString()))
                .andExpect(jsonPath("$.totalApplications").exists());
    }

    @Test
    void export_withJobId_returnsCsv() throws Exception {
        mockMvc.perform(get("/reports/export")
                        .param("jobId", jobId.toString())
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(result -> org.hamcrest.MatcherAssert.assertThat(
                        result.getResponse().getContentType(),
                        org.hamcrest.CoreMatchers.containsString("text/csv")))
                .andExpect(result -> org.hamcrest.MatcherAssert.assertThat(
                        result.getResponse().getContentAsString(),
                        org.hamcrest.CoreMatchers.containsString("applicationId")));
    }
}
