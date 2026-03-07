package com.futurescope.platform.auth.web;

import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.auth.service.CompanyService;
import com.futurescope.platform.auth.web.dto.CompanyMemberResponse;
import com.futurescope.platform.auth.web.dto.CompanyResponse;
import com.futurescope.platform.auth.web.dto.UpdateBrandingRequest;
import com.futurescope.platform.auth.web.dto.UpdateMemberRoleRequest;
import com.futurescope.platform.job.service.PipelineQueryService;
import com.futurescope.platform.job.service.PipelineService;
import com.futurescope.platform.job.web.dto.CreatePipelineRequest;
import com.futurescope.platform.job.web.dto.PipelineResponse;
import jakarta.validation.Valid;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/companies")
public class CompanyController {

    private final CompanyService companyService;
    private final PipelineQueryService pipelineQueryService;
    private final PipelineService pipelineService;

    public CompanyController(
            CompanyService companyService,
            PipelineQueryService pipelineQueryService,
            PipelineService pipelineService
    ) {
        this.companyService = companyService;
        this.pipelineQueryService = pipelineQueryService;
        this.pipelineService = pipelineService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponse> getById(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id
    ) {
        CompanyResponse response = companyService.getById(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/members/me")
    public ResponseEntity<CompanyMemberResponse> getCurrentMember(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id
    ) {
        CompanyMemberResponse response = companyService.getCurrentMember(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<CompanyMemberResponse>> listMembers(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id
    ) {
        List<CompanyMemberResponse> list = companyService.listMembers(id, currentUser);
        return ResponseEntity.ok(list);
    }

    @PatchMapping("/{id}/members/{memberId}/role")
    public ResponseEntity<CompanyMemberResponse> updateMemberRole(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @PathVariable UUID memberId,
            @Valid @RequestBody UpdateMemberRoleRequest request
    ) {
        CompanyMemberResponse response = companyService.updateMemberRole(id, memberId, request, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/members/{userId}/avatar")
    public ResponseEntity<Resource> getMemberAvatar(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @PathVariable UUID userId
    ) {
        Path path = companyService.getMemberAvatarPath(id, userId, currentUser);
        if (path == null) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(path);
        String contentType = path.toString().toLowerCase().endsWith(".png") ? "image/png"
                : path.toString().toLowerCase().endsWith(".webp") ? "image/webp"
                : path.toString().toLowerCase().endsWith(".gif") ? "image/gif"
                : "image/jpeg";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CACHE_CONTROL, "private, max-age=3600")
                .body(resource);
    }

    @PatchMapping("/{id}/branding")
    public ResponseEntity<CompanyResponse> updateBranding(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBrandingRequest request
    ) {
        CompanyResponse response = companyService.updateBranding(id, currentUser, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/pipelines")
    public ResponseEntity<List<PipelineResponse>> listPipelines(
            @AuthenticationPrincipal User currentUser,
            @PathVariable("id") UUID companyId
    ) {
        List<PipelineResponse> list = pipelineQueryService.listByCompany(companyId, currentUser);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/{id}/pipelines")
    public ResponseEntity<PipelineResponse> createPipeline(
            @AuthenticationPrincipal User currentUser,
            @PathVariable("id") UUID companyId,
            @Valid @RequestBody CreatePipelineRequest request
    ) {
        request.setCompanyId(companyId);
        PipelineResponse response = pipelineService.createPipeline(request, currentUser);
        return ResponseEntity.ok(response);
    }
}
