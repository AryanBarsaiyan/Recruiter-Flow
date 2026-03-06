package com.futurescope.platform.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurescope.platform.application.domain.ApplicationAnswer;
import com.futurescope.platform.application.domain.JobApplication;
import com.futurescope.platform.application.repository.ApplicationAnswerRepository;
import com.futurescope.platform.application.repository.JobApplicationRepository;
import com.futurescope.platform.application.web.dto.ApplyForJobRequest;
import com.futurescope.platform.application.web.dto.ApplicationResponse;
import com.futurescope.platform.ai.screening.service.ResumeScreeningService;
import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.auth.repository.UserRepository;
import com.futurescope.platform.candidate.domain.Candidate;
import com.futurescope.platform.candidate.domain.Resume;
import com.futurescope.platform.candidate.repository.CandidateRepository;
import com.futurescope.platform.candidate.repository.ResumeRepository;
import com.futurescope.platform.job.domain.Job;
import com.futurescope.platform.job.repository.JobRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class ApplicationService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;
    private final ResumeRepository resumeRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final ApplicationAnswerRepository applicationAnswerRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    private final ResumeScreeningService resumeScreeningService;
    private final SecureRandom random = new SecureRandom();

    public ApplicationService(
            JobRepository jobRepository,
            UserRepository userRepository,
            CandidateRepository candidateRepository,
            ResumeRepository resumeRepository,
            JobApplicationRepository jobApplicationRepository,
            ApplicationAnswerRepository applicationAnswerRepository,
            PasswordEncoder passwordEncoder,
            ObjectMapper objectMapper,
            ResumeScreeningService resumeScreeningService
    ) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.candidateRepository = candidateRepository;
        this.resumeRepository = resumeRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.applicationAnswerRepository = applicationAnswerRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
        this.resumeScreeningService = resumeScreeningService;
    }

    @Transactional
    public ApplicationResponse apply(UUID jobId, ApplyForJobRequest request) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));

        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseGet(() -> createCandidateUser(request.getEmail()));

        Candidate candidate = candidateRepository.findByUser(user)
                .orElseGet(() -> createCandidateProfile(user, request));

        Resume resume = new Resume();
        resume.setId(UUID.randomUUID());
        resume.setCandidate(candidate);
        resume.setStoragePath(request.getResumeStoragePath());
        resume.setOriginalFilename(request.getResumeOriginalFilename());
        resume.setCreatedAt(OffsetDateTime.now());
        resumeRepository.save(resume);

        JobApplication application = new JobApplication();
        application.setId(UUID.randomUUID());
        application.setJob(job);
        application.setCandidate(candidate);
        application.setResume(resume);
        application.setStatus("applied");
        OffsetDateTime now = OffsetDateTime.now();
        application.setAppliedAt(now);
        application.setLastStatusAt(now);
        application.setSource("job_board");
        jobApplicationRepository.save(application);

        if (request.getAnswers() != null) {
            for (Map.Entry<String, Object> entry : request.getAnswers().entrySet()) {
                ApplicationAnswer answer = new ApplicationAnswer();
                answer.setId(UUID.randomUUID());
                answer.setApplication(application);
                answer.setFieldKey(entry.getKey());
                answer.setValueJson(serializeToJson(entry.getValue()));
                answer.setCreatedAt(now);
                applicationAnswerRepository.save(answer);
            }
        }

        // Trigger mock AI resume screening synchronously for now
        resumeScreeningService.screenApplication(application);

        ApplicationResponse response = new ApplicationResponse();
        response.setId(application.getId());
        response.setJobId(job.getId());
        response.setCandidateId(candidate.getId());
        response.setStatus(application.getStatus());
        response.setAppliedAt(application.getAppliedAt());
        return response;
    }

    private User createCandidateUser(String email) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(generateRandomPassword()));
        user.setUserType("candidate");
        user.setActive(true);
        user.setCreatedAt(OffsetDateTime.now());
        return userRepository.save(user);
    }

    private Candidate createCandidateProfile(User user, ApplyForJobRequest request) {
        Candidate candidate = new Candidate();
        candidate.setId(UUID.randomUUID());
        candidate.setUser(user);
        candidate.setFullName(request.getFullName());
        candidate.setPhone(request.getPhone());
        candidate.setCreatedAt(OffsetDateTime.now());
        return candidateRepository.save(candidate);
    }

    private String generateRandomPassword() {
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String serializeToJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid answer value");
        }
    }
}

