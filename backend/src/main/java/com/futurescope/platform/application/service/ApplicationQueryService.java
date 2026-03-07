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
import com.futurescope.platform.job.domain.Job;
import com.futurescope.platform.job.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
public class ApplicationQueryService {

    private final JobApplicationRepository jobApplicationRepository;
    private final ApplicationStageProgressRepository stageProgressRepository;
    private final CandidateRepository candidateRepository;
    private final JobRepository jobRepository;
    private final RbacService rbacService;
    private final ResumeUploadService resumeUploadService;

    public ApplicationQueryService(
            JobApplicationRepository jobApplicationRepository,
            ApplicationStageProgressRepository stageProgressRepository,
            CandidateRepository candidateRepository,
            JobRepository jobRepository,
            RbacService rbacService,
            ResumeUploadService resumeUploadService
    ) {
        this.jobApplicationRepository = jobApplicationRepository;
        this.stageProgressRepository = stageProgressRepository;
        this.candidateRepository = candidateRepository;
        this.jobRepository = jobRepository;
        this.rbacService = rbacService;
        this.resumeUploadService = resumeUploadService;
    }

    /**
     * Returns resume path and filename for download. Must be called within transactional context
     * so lazy associations (Resume, Job, Company) are loaded.
     */
    @Transactional(readOnly = true)
    public ResumeDownload getResumeForDownload(UUID applicationId, User currentUser) {
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
        if (app.getResume() == null || app.getResume().getStoragePath() == null) {
            throw new IllegalArgumentException("Resume not found");
        }
        Path path = resumeUploadService.resolveResumePath(app.getResume().getStoragePath());
        if (path == null) {
            throw new IllegalArgumentException("Resume file not found");
        }
        String filename = app.getResume().getOriginalFilename() != null
                ? app.getResume().getOriginalFilename()
                : "resume.pdf";
        boolean isTxt = filename.toLowerCase().endsWith(".txt");
        return new ResumeDownload(path, filename, isTxt);
    }

    public record ResumeDownload(Path path, String filename, boolean isTxt) {}

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
        r.setJobTitle(app.getJob().getTitle());
        r.setCompanyName(app.getJob().getCompany() != null ? app.getJob().getCompany().getName() : null);
        r.setCandidateId(app.getCandidate().getId());
        r.setCandidateName(app.getCandidate().getFullName());
        r.setCandidateEmail(app.getCandidate().getUser() != null ? app.getCandidate().getUser().getEmail() : null);
        if (app.getResume() != null) {
            r.setResumeId(app.getResume().getId());
            r.setResumeOriginalFilename(app.getResume().getOriginalFilename());
        }
        r.setStatus(app.getStatus());
        r.setAppliedAt(app.getAppliedAt());
        return r;
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> listByJob(UUID jobId, User currentUser) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));
        rbacService.requireActiveCompanyMember(currentUser, job.getCompany().getId());
        return jobApplicationRepository.findByJobOrderByAppliedAtDesc(job).stream()
                .map(this::toResponse)
                .toList();
    }
}
