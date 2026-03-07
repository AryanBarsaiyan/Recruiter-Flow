package com.futurescope.platform.job.web;

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

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Feature tests: create job (recruiter + RBAC), list public jobs.
 */
class JobsFeatureTest extends AbstractIntegrationTest {

    @Autowired WebApplicationContext webApplicationContext;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired CompanyMemberRepository companyMemberRepository;

    MockMvc mockMvc;
    String recruiterToken;
    java.util.UUID companyId;
    UUID pipelineId;

    String recruiterEmail;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        recruiterEmail = "jobs-" + UUID.randomUUID() + "@test.com";
        String signupBody = objectMapper.writeValueAsString(Map.of(
                "email", recruiterEmail,
                "password", "password1",
                "fullName", "R",
                "companyName", "Jobs Test Co " + UUID.randomUUID().toString().substring(0, 8)));
        String res = mockMvc.perform(post("/auth/signup-super-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        recruiterToken = objectMapper.readTree(res).get("accessToken").asText();
        User user = userRepository.findByEmailIgnoreCase(recruiterEmail).orElseThrow();
        companyId = companyMemberRepository.findByUser(user).stream().findFirst().orElseThrow().getCompany().getId();
        String pipelineBody = objectMapper.writeValueAsString(Map.of(
                "companyId", companyId.toString(),
                "name", "Default Hiring",
                "isDefault", true));
        String pipelineRes = mockMvc.perform(post("/companies/{companyId}/pipelines", companyId)
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pipelineBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        pipelineId = UUID.fromString(objectMapper.readTree(pipelineRes).get("id").asText());
    }

    @Test
    void createJob_withRecruiterToken_returns201Or200() throws Exception {
        String req = objectMapper.writeValueAsString(Map.of(
                "companyId", companyId.toString(),
                "title", "Backend Engineer"));
        mockMvc.perform(post("/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(req))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("Backend Engineer"));
    }

    @Test
    void getJob_byId_asRecruiter_returnsJob() throws Exception {
        String req = objectMapper.writeValueAsString(Map.of(
                "companyId", companyId.toString(),
                "title", "Get Job Test"));
        String createRes = mockMvc.perform(post("/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(req))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        UUID jobId = UUID.fromString(objectMapper.readTree(createRes).get("id").asText());
        mockMvc.perform(get("/jobs/{id}", jobId)
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(jobId.toString()))
                .andExpect(jsonPath("$.title").value("Get Job Test"));
    }

    @Test
    void listJobs_byCompanyId_returnsPage() throws Exception {
        mockMvc.perform(get("/jobs")
                        .param("companyId", companyId.toString())
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").exists());
    }

    @Test
    void updateJob_returnsUpdatedJob() throws Exception {
        String createReq = objectMapper.writeValueAsString(Map.of("companyId", companyId.toString(), "title", "Original"));
        String createRes = mockMvc.perform(post("/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createReq))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        UUID jobId = UUID.fromString(objectMapper.readTree(createRes).get("id").asText());
        mockMvc.perform(put("/jobs/{id}", jobId)
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("title", "Updated Title", "pipelineId", pipelineId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void deleteJob_returnsNoContent() throws Exception {
        String createReq = objectMapper.writeValueAsString(Map.of("companyId", companyId.toString(), "title", "To Delete"));
        String createRes = mockMvc.perform(post("/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createReq))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        UUID jobId = UUID.fromString(objectMapper.readTree(createRes).get("id").asText());
        mockMvc.perform(delete("/jobs/{id}", jobId)
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void getPublicJobs_returnsList() throws Exception {
        mockMvc.perform(get("/jobs/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getPublicJob_byId_returnsJobWhenPublished() throws Exception {
        String createReq = objectMapper.writeValueAsString(Map.of("companyId", companyId.toString(), "title", "Public Job Title"));
        String createRes = mockMvc.perform(post("/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createReq))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        UUID jobId = UUID.fromString(objectMapper.readTree(createRes).get("id").asText());
        mockMvc.perform(put("/jobs/{id}", jobId)
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("published", true, "pipelineId", pipelineId.toString()))))
                .andExpect(status().isOk());
        mockMvc.perform(get("/jobs/public/{id}", jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(jobId.toString()))
                .andExpect(jsonPath("$.title").value("Public Job Title"));
    }

    @Test
    void createJob_withoutPipelineWhenNoDefault_returns400() throws Exception {
        String email = "jobs-nopipe-" + UUID.randomUUID() + "@test.com";
        String signupBody = objectMapper.writeValueAsString(Map.of(
                "email", email,
                "password", "password1",
                "fullName", "R",
                "companyName", "No Pipeline Co " + UUID.randomUUID().toString().substring(0, 8)));
        String res = mockMvc.perform(post("/auth/signup-super-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(res).get("accessToken").asText();
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        UUID noPipelineCompanyId = companyMemberRepository.findByUser(user).stream().findFirst().orElseThrow().getCompany().getId();
        String req = objectMapper.writeValueAsString(Map.of(
                "companyId", noPipelineCompanyId.toString(),
                "title", "No Pipeline Job"));
        mockMvc.perform(post("/jobs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(req))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Pipeline is required. Create a pipeline first in Company settings."));
    }

    @Test
    void updateJob_withoutPipelineId_returns400() throws Exception {
        String createReq = objectMapper.writeValueAsString(Map.of("companyId", companyId.toString(), "title", "To Update"));
        String createRes = mockMvc.perform(post("/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createReq))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        UUID jobId = UUID.fromString(objectMapper.readTree(createRes).get("id").asText());
        mockMvc.perform(put("/jobs/{id}", jobId)
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("title", "Updated"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Pipeline is required"));
    }

    @Test
    void getPublicJob_byId_returns404WhenJobNotFound() throws Exception {
        UUID randomId = UUID.randomUUID();
        mockMvc.perform(get("/jobs/public/{id}", randomId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Job not found"));
    }

    @Test
    void listJobApplications_asRecruiter_returnsApplicationsWithJobTitle() throws Exception {
        String createReq = objectMapper.writeValueAsString(Map.of("companyId", companyId.toString(), "title", "Applications List Job"));
        String createRes = mockMvc.perform(post("/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createReq))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        UUID jobId = UUID.fromString(objectMapper.readTree(createRes).get("id").asText());
        String applyBody = objectMapper.writeValueAsString(Map.of(
                "email", "list-applicant-" + UUID.randomUUID() + "@test.com",
                "fullName", "Applicant",
                "resumeStoragePath", "test/resume.pdf",
                "resumeOriginalFilename", "resume.pdf"));
        mockMvc.perform(post("/jobs/{jobId}/apply", jobId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(applyBody))
                .andExpect(status().isOk());
        mockMvc.perform(get("/jobs/{id}/applications", jobId)
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].jobTitle").value("Applications List Job"))
                .andExpect(jsonPath("$[0].companyName").exists());
    }
}
