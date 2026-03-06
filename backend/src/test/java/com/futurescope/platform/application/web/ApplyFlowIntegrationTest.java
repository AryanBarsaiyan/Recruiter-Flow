package com.futurescope.platform.application.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurescope.platform.AbstractIntegrationTest;
import com.futurescope.platform.auth.domain.Company;
import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.auth.repository.CompanyRepository;
import com.futurescope.platform.auth.repository.UserRepository;
import com.futurescope.platform.job.domain.Job;
import com.futurescope.platform.job.repository.JobRepository;
import com.futurescope.platform.application.web.dto.ApplyForJobRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
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

class ApplyFlowIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    WebApplicationContext webApplicationContext;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    CompanyRepository companyRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    JobRepository jobRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    MockMvc mockMvc;
    Job job;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        Company company = new Company();
        company.setId(UUID.randomUUID());
        company.setName("Test Co");
        company.setSlug("test-co");
        company.setActive(true);
        company.setCreatedAt(OffsetDateTime.now());
        companyRepository.save(company);

        User creator = new User();
        creator.setId(UUID.randomUUID());
        creator.setEmail("creator@test.com");
        creator.setPasswordHash(passwordEncoder.encode("pass"));
        creator.setUserType("recruiter");
        creator.setActive(true);
        creator.setCreatedAt(OffsetDateTime.now());
        userRepository.save(creator);

        job = new Job();
        job.setId(UUID.randomUUID());
        job.setCompany(company);
        job.setTitle("Backend Engineer");
        job.setDescription("Java role");
        job.setCreatedBy(creator);
        job.setPublished(true);
        OffsetDateTime now = OffsetDateTime.now();
        job.setCreatedAt(now);
        job.setUpdatedAt(now);
        jobRepository.save(job);
    }

    @Test
    void apply_createsApplicationAndRunsScreening() throws Exception {
        ApplyForJobRequest request = new ApplyForJobRequest();
        request.setEmail("candidate@test.com");
        request.setFullName("Jane Doe");
        request.setResumeStoragePath("/uploads/resume1.pdf");
        request.setResumeOriginalFilename("resume.pdf");
        request.setAnswers(Map.of("phone", "1234567890"));

        mockMvc.perform(post("/jobs/{jobId}/apply", job.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.jobId").value(job.getId().toString()))
                .andExpect(jsonPath("$.status").value("invited")); // mock AI shortlists
    }
}
