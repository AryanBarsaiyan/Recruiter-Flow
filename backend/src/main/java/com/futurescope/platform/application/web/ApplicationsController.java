package com.futurescope.platform.application.web;

import com.futurescope.platform.application.service.ApplicationQueryService;
import com.futurescope.platform.application.service.ApplicationQueryService.ResumeDownload;
import com.futurescope.platform.application.web.dto.ApplicationResponse;
import com.futurescope.platform.application.web.dto.StageProgressResponse;
import com.futurescope.platform.auth.domain.User;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/applications")
public class ApplicationsController {

    private final ApplicationQueryService applicationQueryService;

    public ApplicationsController(ApplicationQueryService applicationQueryService) {
        this.applicationQueryService = applicationQueryService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getById(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id
    ) {
        ApplicationResponse response = applicationQueryService.getById(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/stage")
    public ResponseEntity<List<StageProgressResponse>> getStageProgress(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id
    ) {
        List<StageProgressResponse> list = applicationQueryService.getStageProgress(id, currentUser);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}/resume")
    public ResponseEntity<Resource> getResume(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id
    ) {
        ResumeDownload download = applicationQueryService.getResumeForDownload(id, currentUser);
        Resource resource = new FileSystemResource(download.path());
        MediaType contentType = download.isTxt() ? MediaType.TEXT_PLAIN : MediaType.APPLICATION_PDF;
        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + download.filename() + "\"")
                .body(resource);
    }
}
