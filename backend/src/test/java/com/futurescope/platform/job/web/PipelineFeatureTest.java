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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PipelineFeatureTest extends AbstractIntegrationTest {

    @Autowired WebApplicationContext webApplicationContext;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired CompanyMemberRepository companyMemberRepository;

    MockMvc mockMvc;
    String recruiterToken;
    UUID companyId;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        String email = "pipeline-" + UUID.randomUUID() + "@test.com";
        String body = objectMapper.writeValueAsString(Map.of(
                "email", email,
                "password", "password1",
                "fullName", "Rec",
                "companyName", "Pipeline Co " + UUID.randomUUID().toString().substring(0, 8)));
        String res = mockMvc.perform(post("/auth/signup-super-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        recruiterToken = objectMapper.readTree(res).get("accessToken").asText();
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        companyId = companyMemberRepository.findByUser(user).stream().findFirst().orElseThrow().getCompany().getId();
    }

    @Test
    void listPipelines_byCompany_returns200AndArray() throws Exception {
        mockMvc.perform(get("/companies/{companyId}/pipelines", companyId)
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void createPipeline_validRequest_returns201AndPipelineWithStages() throws Exception {
        String createBody = objectMapper.writeValueAsString(Map.of(
                "companyId", companyId.toString(),
                "name", "Default Hiring",
                "isDefault", true));
        String res = mockMvc.perform(post("/companies/{companyId}/pipelines", companyId)
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Default Hiring"))
                .andExpect(jsonPath("$.companyId").value(companyId.toString()))
                .andExpect(jsonPath("$.default").value(true))
                .andReturn().getResponse().getContentAsString();
        String pipelineId = objectMapper.readTree(res).get("id").asText();

        mockMvc.perform(get("/pipelines/{pipelineId}/stages", pipelineId)
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value("Resume Screening"))
                .andExpect(jsonPath("$[0].type").value("resume_screening"))
                .andExpect(jsonPath("$[1].name").value("AI Interview"))
                .andExpect(jsonPath("$[2].name").value("Offer"));
    }

    @Test
    void addStage_validRequest_returns200AndStage() throws Exception {
        String createBody = objectMapper.writeValueAsString(Map.of(
                "companyId", companyId.toString(),
                "name", "Test Pipeline",
                "isDefault", false));
        String createRes = mockMvc.perform(post("/companies/{companyId}/pipelines", companyId)
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String pipelineId = objectMapper.readTree(createRes).get("id").asText();

        String addStageBody = objectMapper.writeValueAsString(Map.of(
                "name", "Phone Screen",
                "type", "manual_interview"));
        mockMvc.perform(post("/pipelines/{pipelineId}/stages", pipelineId)
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addStageBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Phone Screen"))
                .andExpect(jsonPath("$.type").value("manual_interview"))
                .andExpect(jsonPath("$.pipelineId").value(pipelineId));

        mockMvc.perform(get("/pipelines/{pipelineId}/stages", pipelineId)
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    void reorderStages_validRequest_returns200AndNewOrder() throws Exception {
        String createBody = objectMapper.writeValueAsString(Map.of(
                "companyId", companyId.toString(),
                "name", "Reorder Pipeline",
                "isDefault", false));
        String createRes = mockMvc.perform(post("/companies/{companyId}/pipelines", companyId)
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String pipelineId = objectMapper.readTree(createRes).get("id").asText();
        String stagesRes = mockMvc.perform(get("/pipelines/{pipelineId}/stages", pipelineId)
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var stages = objectMapper.readTree(stagesRes);
        String id0 = stages.get(0).get("id").asText();
        String id1 = stages.get(1).get("id").asText();
        String id2 = stages.get(2).get("id").asText();
        String reorderBody = objectMapper.writeValueAsString(Map.of("stageIds", java.util.List.of(id2, id0, id1)));
        mockMvc.perform(put("/pipelines/{pipelineId}/stages/reorder", pipelineId)
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reorderBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value("Offer"))
                .andExpect(jsonPath("$[1].name").value("Resume Screening"))
                .andExpect(jsonPath("$[2].name").value("AI Interview"));
    }

    @Test
    void getStages_invalidPipelineId_returns400() throws Exception {
        UUID randomPipelineId = UUID.randomUUID();
        mockMvc.perform(get("/pipelines/{pipelineId}/stages", randomPipelineId)
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isBadRequest());
    }
}
