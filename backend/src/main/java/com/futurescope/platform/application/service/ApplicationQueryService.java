package com.futurescope.platform.application.service;

import com.futurescope.platform.application.domain.ApplicationStageProgress;
import com.futurescope.platform.application.domain.JobApplication;
import com.futurescope.platform.application.repository.ApplicationStageProgressRepository;
import com.futurescope.platform.application.repository.JobApplicationRepository;
import com.futurescope.platform.application.web.dto.ApplicationResponse;
import com.futurescope.platform.application.web.dto.StageProgressResponse;
import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.auth.service.RbacService;
import com.futurescope.platform.candidate.domain.Candidate;
import com.futurescope.platform.candidate.repository.CandidateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ApplicationQueryService {

    private final JobApplicationRepository jobApplicationRepository;
    private final ApplicationStageProgressRepository stageProgressRepository;
    private final CandidateRepository candidateRepository;
    private final RbacService rbacService;

    public ApplicationQueryService(
            JobApplicationRepository jobApplicationRepository,
            ApplicationStageProgressRepository stageProgressRepository,
            CandidateRepository candidateRepository,
            RbacService rbacService
    ) {
        this.jobApplicationRepository = jobApplicationRepository;
        this.stageProgressRepository = stageProgressRepository;
        this.candidateRepository = candidateRepository;
        this.rbacService = rbacService;
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getById(UUID applicationId, User currentUser) {
        JobApplication app = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        if ("candidate".equals(currentUser.getUserType())) {
            Candidate candidate = candidateRepository.findByUser(currentUser)
                    .orElseThrow(() -> new IllegalArgumentException("Candidate profile not found"));
            if (!app.getCandidate().getId().equals(candidate.getId())) {
                throw new IllegalArgumentException("Application not found");
            }
        } else {
            rbacService.requireActiveCompanyMember(currentUser, app.getJob().getCompany().getId());
        }
        return toResponse(app);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> listForCandidate(User currentUser) {
        Candidate candidate = candidateRepository.findByUser(currentUser)
                .orElseThrow(() -> new IllegalArgumentException("Candidate profile not found"));
        return jobApplicationRepository.findByCandidateOrderByAppliedAtDesc(candidate).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StageProgressResponse> getStageProgress(UUID applicationId, User currentUser) {
        JobApplication app = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        if ("candidate".equals(currentUser.getUserType())) {
            Candidate candidate = candidateRepository.findByUser(currentUser)
                    .orElseThrow(() -> new IllegalArgumentException("Candidate profile not found"));
            if (!app.getCandidate().getId().equals(candidate.getId())) {
                throw new IllegalArgumentException("Application not found");
            }
        } else {
            rbacService.requireActiveCompanyMember(currentUser, app.getJob().getCompany().getId());
        }
        return stageProgressRepository.findByApplication(app).stream()
                .map(this::toStageResponse)
                .toList();
    }

    private StageProgressResponse toStageResponse(ApplicationStageProgress p) {
        StageProgressResponse r = new StageProgressResponse();
        r.setId(p.getId());
        r.setStageId(p.getStage().getId());
        r.setStageName(p.getStage().getName());
        r.setStatus(p.getStatus());
        r.setStartedAt(p.getStartedAt());
        r.setCompletedAt(p.getCompletedAt());
        r.setNotes(p.getNotes());
        return r;
    }

    private ApplicationResponse toResponse(JobApplication app) {
        ApplicationResponse r = new ApplicationResponse();
        r.setId(app.getId());
        r.setJobId(app.getJob().getId());
        r.setCandidateId(app.getCandidate().getId());
        r.setStatus(app.getStatus());
        r.setAppliedAt(app.getAppliedAt());
        return r;
    }
}
