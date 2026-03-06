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
    void getStages_invalidPipelineId_returns400() throws Exception {
        UUID randomPipelineId = UUID.randomUUID();
        mockMvc.perform(get("/pipelines/{pipelineId}/stages", randomPipelineId)
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isBadRequest());
    }
}
