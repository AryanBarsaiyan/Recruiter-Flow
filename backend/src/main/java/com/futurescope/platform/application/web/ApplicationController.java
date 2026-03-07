package com.futurescope.platform.application.web;

import com.futurescope.platform.application.service.ApplicationService;
import com.futurescope.platform.application.service.ResumeUploadService;
import com.futurescope.platform.application.web.dto.ApplyForJobRequest;
import com.futurescope.platform.application.web.dto.ApplicationResponse;
import com.futurescope.platform.application.web.dto.UploadResumeResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/jobs")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final ResumeUploadService resumeUploadService;

    public ApplicationController(ApplicationService applicationService, ResumeUploadService resumeUploadService) {
        this.applicationService = applicationService;
        this.resumeUploadService = resumeUploadService;
    }

    @PostMapping("/{jobId}/upload-resume")
    public ResponseEntity<UploadResumeResponse> uploadResume(
            @PathVariable UUID jobId,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        UploadResumeResponse response = resumeUploadService.uploadResume(jobId, file);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{jobId}/apply")
    public ResponseEntity<ApplicationResponse> apply(
            @PathVariable UUID jobId,
            @Valid @RequestBody ApplyForJobRequest request
    ) {
        ApplicationResponse response = applicationService.apply(jobId, request);
        return ResponseEntity.ok(response);
    }
}

