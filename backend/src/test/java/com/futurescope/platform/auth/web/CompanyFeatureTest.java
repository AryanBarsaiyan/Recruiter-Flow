package com.futurescope.platform.auth.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurescope.platform.AbstractIntegrationTest;
import com.futurescope.platform.auth.domain.Company;
import com.futurescope.platform.auth.domain.CompanyMember;
import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.auth.repository.CompanyMemberRepository;
import com.futurescope.platform.auth.repository.CompanyRepository;
import com.futurescope.platform.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CompanyFeatureTest extends AbstractIntegrationTest {

    @Autowired WebApplicationContext webApplicationContext;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired CompanyMemberRepository companyMemberRepository;
    @Autowired CompanyRepository companyRepository;

    MockMvc mockMvc;
    String recruiterToken;
    UUID companyId;
    UUID pipelineId;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        String email = "company-" + UUID.randomUUID() + "@test.com";
        String body = objectMapper.writeValueAsString(Map.of(
                "email", email,
                "password", "password1",
                "fullName", "Rec",
                "companyName", "Company Co " + UUID.randomUUID().toString().substring(0, 8)));
        String res = mockMvc.perform(post("/auth/signup-super-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        recruiterToken = objectMapper.readTree(res).get("accessToken").asText();
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        companyId = companyMemberRepository.findByUser(user).stream().findFirst().orElseThrow().getCompany().getId();
        pipelineId = createPipeline(companyId, recruiterToken);
    }

    private UUID createPipeline(UUID companyId, String token) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "companyId", companyId.toString(),
                "name", "Default Hiring",
                "isDefault", true));
        String res = mockMvc.perform(post("/companies/{companyId}/pipelines", companyId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(res).get("id").asText());
    }

    @Test
    void getCompany_byId_returnsCompany() throws Exception {
        mockMvc.perform(get("/companies/{id}", companyId)
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(companyId.toString()))
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.slug").exists());
    }

    @Test
    void getCurrentMember_returnsOwnMembership() throws Exception {
        mockMvc.perform(get("/companies/{id}/members/me", companyId)
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleName").exists())
                .andExpect(jsonPath("$.userEmail").exists());
    }

    @Test
    void listMembers_returnsMembersWithAvatarAndFullName() throws Exception {
        mockMvc.perform(get("/companies/{id}/members", companyId)
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].roleName").exists())
                .andExpect(jsonPath("$[0].userId").exists())
                .andExpect(jsonPath("$[0].userEmail").exists())
                .andExpect(jsonPath("$[0].fullName").exists());
    }

    @Test
    void updateMemberRole_adminCanChangeRole() throws Exception {
        String inviteEmail = "readonly-" + UUID.randomUUID() + "@test.com";
        String inviteRes = mockMvc.perform(post("/auth/invite")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "companyId", companyId.toString(),
                                "email", inviteEmail,
                                "roleName", "ReadOnly"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(inviteRes).get("inviteToken").asText();

        mockMvc.perform(post("/auth/accept-invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", token,
                                "password", "pass123",
                                "fullName", "ReadOnly User"))))
                .andExpect(status().isOk());

        User readonlyUser = userRepository.findByEmailIgnoreCase(inviteEmail).orElseThrow();
        CompanyMember readonlyMember = companyMemberRepository.findByCompanyIdAndUserId(companyId, readonlyUser.getId()).orElseThrow();

        mockMvc.perform(patch("/companies/{id}/members/{memberId}/role", companyId, readonlyMember.getId())
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("roleName", "Admin"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleName").value("Admin"));
    }

    @Test
    @Transactional
    void updateMemberRole_readOnlyCannotChangeRole() throws Exception {
        String inviteEmail = "readonly2-" + UUID.randomUUID() + "@test.com";
        String inviteRes = mockMvc.perform(post("/auth/invite")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "companyId", companyId.toString(),
                                "email", inviteEmail,
                                "roleName", "ReadOnly"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(inviteRes).get("inviteToken").asText();

        String acceptRes = mockMvc.perform(post("/auth/accept-invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", token,
                                "password", "pass123",
                                "fullName", "ReadOnly User"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String readonlyToken = objectMapper.readTree(acceptRes).get("accessToken").asText();

        Company company = companyRepository.findById(companyId).orElseThrow();
        var members = companyMemberRepository.findByCompanyOrderByCreatedAtDesc(company);
        CompanyMember superAdminMember = members.stream()
                .filter(m -> "SuperAdmin".equals(m.getRole().getName()))
                .findFirst()
                .orElseThrow();

        mockMvc.perform(patch("/companies/{id}/members/{memberId}/role", companyId, superAdminMember.getId())
                        .header("Authorization", "Bearer " + readonlyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("roleName", "Admin"))))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getMemberAvatar_noAvatar_returns404() throws Exception {
        Company company = companyRepository.findById(companyId).orElseThrow();
        CompanyMember member = companyMemberRepository.findByCompanyOrderByCreatedAtDesc(company).get(0);
        UUID memberUserId = member.getUser().getId();

        mockMvc.perform(get("/companies/{id}/members/{userId}/avatar", companyId, memberUserId)
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateBranding_returnsUpdatedCompany() throws Exception {
        mockMvc.perform(patch("/companies/{id}/branding", companyId)
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("brandingConfigJson", "{\"primary\":\"#000\"}"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.brandingConfigJson").value("{\"primary\":\"#000\"}"));
    }

    @Test
    void getRecruiterDashboard_returnsStats() throws Exception {
        mockMvc.perform(get("/dashboards/recruiter")
                        .param("companyId", companyId.toString())
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyId").value(companyId.toString()))
                .andExpect(jsonPath("$.totalJobs").exists())
                .andExpect(jsonPath("$.totalApplications").exists());
    }

    @Test
    void createJob_adminCanCreateJob() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "companyId", companyId.toString(),
                "title", "Test Job " + UUID.randomUUID()));
        mockMvc.perform(post("/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").exists());
    }

    @Test
    void updateJob_adminCanUpdateJob() throws Exception {
        String createBody = objectMapper.writeValueAsString(Map.of(
                "companyId", companyId.toString(),
                "title", "Original Job"));
        String createRes = mockMvc.perform(post("/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        UUID jobId = UUID.fromString(objectMapper.readTree(createRes).get("id").asText());

        String updateBody = objectMapper.writeValueAsString(Map.of(
                "companyId", companyId.toString(),
                "title", "Updated Job",
                "pipelineId", pipelineId.toString()));
        mockMvc.perform(put("/jobs/{id}", jobId)
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Job"));
    }

    @Test
    void deleteJob_adminCanDeleteJob() throws Exception {
        String createBody = objectMapper.writeValueAsString(Map.of(
                "companyId", companyId.toString(),
                "title", "Job To Delete"));
        String createRes = mockMvc.perform(post("/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        UUID jobId = UUID.fromString(objectMapper.readTree(createRes).get("id").asText());

        mockMvc.perform(delete("/jobs/{id}", jobId)
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void listBulkImportBatches_readOnlyCanList() throws Exception {
        String inviteEmail = "readonly-bulk-" + UUID.randomUUID() + "@test.com";
        String inviteRes = mockMvc.perform(post("/auth/invite")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "companyId", companyId.toString(),
                                "email", inviteEmail,
                                "roleName", "ReadOnly"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(inviteRes).get("inviteToken").asText();

        String acceptRes = mockMvc.perform(post("/auth/accept-invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", token,
                                "password", "pass123",
                                "fullName", "ReadOnly User"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String readonlyToken = objectMapper.readTree(acceptRes).get("accessToken").asText();

        mockMvc.perform(get("/bulk-import/batches")
                        .param("companyId", companyId.toString())
                        .header("Authorization", "Bearer " + readonlyToken))
                .andExpect(status().isOk());
    }

    @Test
    @Transactional
    void readOnlyCannot_createJob() throws Exception {
        String inviteEmail = "readonly-job-" + UUID.randomUUID() + "@test.com";
        String inviteRes = mockMvc.perform(post("/auth/invite")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "companyId", companyId.toString(),
                                "email", inviteEmail,
                                "roleName", "ReadOnly"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(inviteRes).get("inviteToken").asText();

        String acceptRes = mockMvc.perform(post("/auth/accept-invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", token,
                                "password", "pass123",
                                "fullName", "ReadOnly User"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String readonlyToken = objectMapper.readTree(acceptRes).get("accessToken").asText();

        String body = objectMapper.writeValueAsString(Map.of(
                "companyId", companyId.toString(),
                "title", "Test Job"));
        mockMvc.perform(post("/jobs")
                        .header("Authorization", "Bearer " + readonlyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @Transactional
    void readOnlyCannot_updateJob() throws Exception {
        String createBody = objectMapper.writeValueAsString(Map.of(
                "companyId", companyId.toString(),
                "title", "Original Job"));
        String createRes = mockMvc.perform(post("/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        UUID jobId = UUID.fromString(objectMapper.readTree(createRes).get("id").asText());

        String inviteEmail = "readonly-update-" + UUID.randomUUID() + "@test.com";
        String inviteRes = mockMvc.perform(post("/auth/invite")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "companyId", companyId.toString(),
                                "email", inviteEmail,
                                "roleName", "ReadOnly"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(inviteRes).get("inviteToken").asText();

        String acceptRes = mockMvc.perform(post("/auth/accept-invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", token,
                                "password", "pass123",
                                "fullName", "ReadOnly User"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String readonlyToken = objectMapper.readTree(acceptRes).get("accessToken").asText();

        String updateBody = objectMapper.writeValueAsString(Map.of(
                "companyId", companyId.toString(),
                "title", "Hacked Title",
                "pipelineId", pipelineId.toString()));
        mockMvc.perform(put("/jobs/{id}", jobId)
                        .header("Authorization", "Bearer " + readonlyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @Transactional
    void readOnlyCannot_updateBranding() throws Exception {
        String inviteEmail = "readonly-brand-" + UUID.randomUUID() + "@test.com";
        String inviteRes = mockMvc.perform(post("/auth/invite")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "companyId", companyId.toString(),
                                "email", inviteEmail,
                                "roleName", "ReadOnly"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(inviteRes).get("inviteToken").asText();

        String acceptRes = mockMvc.perform(post("/auth/accept-invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", token,
                                "password", "pass123",
                                "fullName", "ReadOnly User"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String readonlyToken = objectMapper.readTree(acceptRes).get("accessToken").asText();

        mockMvc.perform(patch("/companies/{id}/branding", companyId)
                        .header("Authorization", "Bearer " + readonlyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("brandingConfigJson", "{\"primary\":\"#000\"}"))))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @Transactional
    void readOnlyCannot_deleteJob() throws Exception {
        String createBody = objectMapper.writeValueAsString(Map.of(
                "companyId", companyId.toString(),
                "title", "Job To Delete"));
        String createRes = mockMvc.perform(post("/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        UUID jobId = UUID.fromString(objectMapper.readTree(createRes).get("id").asText());

        String inviteEmail = "readonly-del-" + UUID.randomUUID() + "@test.com";
        String inviteRes = mockMvc.perform(post("/auth/invite")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "companyId", companyId.toString(),
                                "email", inviteEmail,
                                "roleName", "ReadOnly"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(inviteRes).get("inviteToken").asText();

        String acceptRes = mockMvc.perform(post("/auth/accept-invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", token,
                                "password", "pass123",
                                "fullName", "ReadOnly User"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String readonlyToken = objectMapper.readTree(acceptRes).get("accessToken").asText();

        mockMvc.perform(delete("/jobs/{id}", jobId)
                        .header("Authorization", "Bearer " + readonlyToken))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @Transactional
    void readOnlyCannot_saveWebhookConfig() throws Exception {
        String inviteEmail = "readonly-webhook-" + UUID.randomUUID() + "@test.com";
        String inviteRes = mockMvc.perform(post("/auth/invite")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "companyId", companyId.toString(),
                                "email", inviteEmail,
                                "roleName", "ReadOnly"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(inviteRes).get("inviteToken").asText();

        String acceptRes = mockMvc.perform(post("/auth/accept-invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", token,
                                "password", "pass123",
                                "fullName", "ReadOnly User"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String readonlyToken = objectMapper.readTree(acceptRes).get("accessToken").asText();

        mockMvc.perform(post("/webhooks/config")
                        .header("Authorization", "Bearer " + readonlyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("companyId", companyId.toString()))))
                .andExpect(status().is4xxClientError());
    }
}
