package com.futurescope.platform.application.web;

import com.futurescope.platform.application.service.ApplicationService;
import com.futurescope.platform.application.web.dto.ApplyForJobRequest;
import com.futurescope.platform.application.web.dto.ApplicationResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/jobs")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
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

