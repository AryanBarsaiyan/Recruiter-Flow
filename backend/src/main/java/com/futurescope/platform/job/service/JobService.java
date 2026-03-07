package com.futurescope.platform.job.service;

import com.futurescope.platform.auth.domain.Company;
import com.futurescope.platform.auth.domain.CompanyMember;
import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.auth.repository.CompanyRepository;
import com.futurescope.platform.auth.repository.UserRepository;
import com.futurescope.platform.auth.service.RbacService;
import com.futurescope.platform.common.exception.ResourceNotFoundException;
import com.futurescope.platform.job.domain.Job;
import com.futurescope.platform.job.domain.Pipeline;
import com.futurescope.platform.job.repository.JobRepository;
import com.futurescope.platform.job.repository.PipelineRepository;
import com.futurescope.platform.job.web.dto.CreateJobRequest;
import com.futurescope.platform.job.web.dto.JobResponse;
import com.futurescope.platform.job.web.dto.UpdateJobRequest;
import com.futurescope.platform.audit.service.AuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final PipelineRepository pipelineRepository;
    private final RbacService rbacService;
    private final AuditService auditService;

    public JobService(
            JobRepository jobRepository,
            CompanyRepository companyRepository,
            UserRepository userRepository,
            PipelineRepository pipelineRepository,
            RbacService rbacService,
            AuditService auditService
    ) {
        this.jobRepository = jobRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.pipelineRepository = pipelineRepository;
        this.rbacService = rbacService;
        this.auditService = auditService;
    }

    @Transactional
    public JobResponse createJob(CreateJobRequest request, UUID createdByUserId) {
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
        User createdBy = userRepository.findById(createdByUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        CompanyMember member = rbacService.requireActiveCompanyMember(createdBy, company.getId());
        rbacService.requireAnyRole(member, java.util.Set.of("SuperAdmin", "Admin"));

        Job job = new Job();
        job.setId(UUID.randomUUID());
        job.setCompany(company);
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());
        job.setEmploymentType(request.getEmploymentType());
        job.setPublished(false);
        job.setApplicationDeadline(request.getApplicationDeadline());
        job.setMaxApplications(request.getMaxApplications());
        job.setResumeCriteriaJson(request.getResumeCriteriaJson());
        job.setCustomFormSchemaJson(request.getCustomFormSchemaJson());
        job.setScoringWeightsOverrideJson(request.getScoringWeightsOverrideJson());
        job.setCreatedBy(createdBy);

        Pipeline pipeline;
        if (request.getPipelineId() != null) {
            pipeline = pipelineRepository.findById(request.getPipelineId())
                    .orElseThrow(() -> new IllegalArgumentException("Pipeline not found"));
            if (!pipeline.getCompany().getId().equals(company.getId())) {
                throw new IllegalArgumentException("Pipeline not found");
            }
        } else {
            pipeline = pipelineRepository.findByCompanyAndIsDefaultTrue(company)
                    .orElseThrow(() -> new IllegalArgumentException("Pipeline is required. Create a pipeline first in Company settings."));
        }
        job.setPipeline(pipeline);

        OffsetDateTime now = OffsetDateTime.now();
        job.setCreatedAt(now);
        job.setUpdatedAt(now);

        Job saved = jobRepository.save(job);
        auditService.log(company, createdBy, member.getRole().getName(), "job_created", "job", saved.getId(), "{\"title\":\"" + saved.getTitle() + "\"}");
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public JobResponse getById(UUID jobId, User currentUser) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));
        CompanyMember member = rbacService.requireActiveCompanyMember(currentUser, job.getCompany().getId());
        rbacService.requireAnyRole(member, java.util.Set.of("SuperAdmin", "Admin", "ReadOnly", "View"));
        return toResponse(job);
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> listByCompany(UUID companyId, User currentUser, Pageable pageable) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
        rbacService.requireActiveCompanyMember(currentUser, company.getId());
        return jobRepository.findByCompanyOrderByCreatedAtDesc(company, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<JobResponse> listPublicJobs() {
        List<Job> jobs = jobRepository.findPublicOpenJobs(OffsetDateTime.now());
        return jobs.stream().map(this::toPublicResponse).toList();
    }

    @Transactional(readOnly = true)
    public JobResponse getPublicJobById(UUID jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        if (!job.isPublished()) {
            throw new ResourceNotFoundException("Job not found");
        }
        OffsetDateTime now = OffsetDateTime.now();
        if (job.getApplicationDeadline() != null && job.getApplicationDeadline().isBefore(now)) {
            throw new ResourceNotFoundException("Job not found");
        }
        return toPublicResponse(job);
    }

    @Transactional
    public JobResponse updateJob(UUID jobId, UpdateJobRequest request, User currentUser) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));
        CompanyMember member = rbacService.requireActiveCompanyMember(currentUser, job.getCompany().getId());
        rbacService.requireAnyRole(member, java.util.Set.of("SuperAdmin", "Admin"));

        if (request.getTitle() != null) job.setTitle(request.getTitle());
        if (request.getDescription() != null) job.setDescription(request.getDescription());
        if (request.getLocation() != null) job.setLocation(request.getLocation());
        if (request.getEmploymentType() != null) job.setEmploymentType(request.getEmploymentType());
        if (request.getPublished() != null) job.setPublished(request.getPublished());
        if (request.getApplicationDeadline() != null) job.setApplicationDeadline(request.getApplicationDeadline());
        if (request.getMaxApplications() != null) job.setMaxApplications(request.getMaxApplications());
        if (request.getResumeCriteriaJson() != null) job.setResumeCriteriaJson(request.getResumeCriteriaJson());
        if (request.getCustomFormSchemaJson() != null) job.setCustomFormSchemaJson(request.getCustomFormSchemaJson());
        if (request.getScoringWeightsOverrideJson() != null) job.setScoringWeightsOverrideJson(request.getScoringWeightsOverrideJson());
        if (request.getPipelineId() == null) {
            throw new IllegalArgumentException("Pipeline is required");
        }
        Pipeline pipeline = pipelineRepository.findById(request.getPipelineId())
                .orElseThrow(() -> new IllegalArgumentException("Pipeline not found"));
        if (!pipeline.getCompany().getId().equals(job.getCompany().getId())) {
            throw new IllegalArgumentException("Pipeline not found");
        }
        job.setPipeline(pipeline);

        job.setUpdatedAt(OffsetDateTime.now());
        Job saved = jobRepository.save(job);
        auditService.log(job.getCompany(), currentUser, member.getRole().getName(), "job_updated", "job", saved.getId(), "{\"title\":\"" + saved.getTitle() + "\"}");
        return toResponse(saved);
    }

    @Transactional
    public void deleteJob(UUID jobId, User currentUser) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));
        CompanyMember member = rbacService.requireActiveCompanyMember(currentUser, job.getCompany().getId());
        rbacService.requireAnyRole(member, java.util.Set.of("SuperAdmin", "Admin"));
        jobRepository.delete(job);
    }

    private JobResponse toResponse(Job job) {
        JobResponse resp = new JobResponse();
        resp.setId(job.getId());
        resp.setCompanyId(job.getCompany().getId());
        resp.setTitle(job.getTitle());
        resp.setDescription(job.getDescription());
        resp.setLocation(job.getLocation());
        resp.setEmploymentType(job.getEmploymentType());
        resp.setPublished(job.isPublished());
        resp.setApplicationDeadline(job.getApplicationDeadline());
        resp.setCustomFormSchemaJson(job.getCustomFormSchemaJson());
        if (job.getPipeline() != null) {
            resp.setPipelineId(job.getPipeline().getId());
            resp.setPipelineName(job.getPipeline().getName());
        }
        return resp;
    }

    private JobResponse toPublicResponse(Job job) {
        JobResponse resp = toResponse(job);
        if (job.getCompany() != null) {
            resp.setCompanyName(job.getCompany().getName());
            resp.setBrandingConfigJson(job.getCompany().getBrandingConfigJson());
        }
        return resp;
    }
}

