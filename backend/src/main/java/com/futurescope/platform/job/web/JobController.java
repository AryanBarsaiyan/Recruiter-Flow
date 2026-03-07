package com.futurescope.platform.job.web;

import com.futurescope.platform.application.service.ApplicationQueryService;
import com.futurescope.platform.application.web.dto.ApplicationResponse;
import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.job.service.JobService;
import com.futurescope.platform.job.web.dto.CreateJobRequest;
import com.futurescope.platform.job.web.dto.JobResponse;
import com.futurescope.platform.job.web.dto.UpdateJobRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/jobs")
public class JobController {

    private final JobService jobService;
    private final ApplicationQueryService applicationQueryService;

    public JobController(JobService jobService, ApplicationQueryService applicationQueryService) {
        this.jobService = jobService;
        this.applicationQueryService = applicationQueryService;
    }

    @PostMapping
    public ResponseEntity<JobResponse> createJob(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CreateJobRequest request
    ) {
        JobResponse response = jobService.createJob(request, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJob(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id
    ) {
        JobResponse response = jobService.getById(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/applications")
    public ResponseEntity<List<ApplicationResponse>> listJobApplications(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id
    ) {
        List<ApplicationResponse> list = applicationQueryService.listByJob(id, currentUser);
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobResponse> updateJob(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @RequestBody UpdateJobRequest request
    ) {
        JobResponse response = jobService.updateJob(id, request, currentUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id
    ) {
        jobService.deleteJob(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<JobResponse>> listJobs(
            @AuthenticationPrincipal User currentUser,
            @RequestParam UUID companyId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<JobResponse> page = jobService.listByCompany(companyId, currentUser, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/public")
    public ResponseEntity<List<JobResponse>> publicJobs() {
        List<JobResponse> jobs = jobService.listPublicJobs();
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<JobResponse> getPublicJob(@PathVariable UUID id) {
        JobResponse response = jobService.getPublicJobById(id);
        return ResponseEntity.ok(response);
    }
}

