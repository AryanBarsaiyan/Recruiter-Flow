package com.futurescope.platform.application.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurescope.platform.AbstractIntegrationTest;
import com.futurescope.platform.auth.domain.Company;
import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.auth.repository.CompanyRepository;
import com.futurescope.platform.auth.repository.UserRepository;
import com.futurescope.platform.job.domain.Job;
import com.futurescope.platform.job.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = "app.resume-upload.dir=./build/resume-upload-test")
class ResumeUploadIntegrationTest extends AbstractIntegrationTest {

    @Autowired WebApplicationContext webApplicationContext;
    @Autowired CompanyRepository companyRepository;
    @Autowired UserRepository userRepository;
    @Autowired JobRepository jobRepository;
    @Autowired PasswordEncoder passwordEncoder;

    MockMvc mockMvc;
    Job job;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        String slugSuffix = UUID.randomUUID().toString().substring(0, 8);
        Company company = new Company();
        company.setId(UUID.randomUUID());
        company.setName("Upload Test Co " + slugSuffix);
        company.setSlug("upload-test-co-" + slugSuffix);
        company.setActive(true);
        company.setCreatedAt(OffsetDateTime.now());
        company.setCreatedAt(OffsetDateTime.now());
        companyRepository.save(company);

        String unique = UUID.randomUUID().toString().substring(0, 8);
        User creator = new User();
        creator.setId(UUID.randomUUID());
        creator.setEmail("upload-creator-" + unique + "@test.com");
        creator.setPasswordHash(passwordEncoder.encode("pass"));
        creator.setUserType("recruiter");
        creator.setActive(true);
        creator.setCreatedAt(OffsetDateTime.now());
        userRepository.save(creator);

        job = new Job();
        job.setId(UUID.randomUUID());
        job.setCompany(company);
        job.setTitle("Upload Test Job");
        job.setCreatedBy(creator);
        job.setPublished(true);
        OffsetDateTime now = OffsetDateTime.now();
        job.setCreatedAt(now);
        job.setUpdatedAt(now);
        jobRepository.save(job);
    }

    @Test
    void uploadResume_returnsStoragePathAndExtractedFields() throws Exception {
        String textContent = "John Doe\njohn@example.com\n+1 555-123-4567";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.txt",
                MediaType.TEXT_PLAIN_VALUE,
                textContent.getBytes());

        mockMvc.perform(multipart("/jobs/{jobId}/upload-resume", job.getId()).file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storagePath").value(org.hamcrest.Matchers.startsWith(job.getId().toString() + "/")))
                .andExpect(jsonPath("$.storagePath").value(org.hamcrest.Matchers.endsWith("_resume.txt")))
                .andExpect(jsonPath("$.originalFilename").value("resume.txt"))
                .andExpect(jsonPath("$.extractedFullName").value("John Doe"))
                .andExpect(jsonPath("$.extractedEmail").value("john@example.com"))
                .andExpect(jsonPath("$.extractedPhone").exists());
    }

    @Test
    void uploadResume_invalidJob_returnsBadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.txt", MediaType.TEXT_PLAIN_VALUE, "content".getBytes());
        UUID randomJobId = UUID.randomUUID();

        mockMvc.perform(multipart("/jobs/{jobId}/upload-resume", randomJobId).file(file))
                .andExpect(status().isBadRequest());
    }
}
