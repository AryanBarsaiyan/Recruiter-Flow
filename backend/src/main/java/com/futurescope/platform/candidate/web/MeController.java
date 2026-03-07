package com.futurescope.platform.candidate.web;

import com.futurescope.platform.application.service.ApplicationQueryService;
import com.futurescope.platform.application.web.dto.ApplicationResponse;
import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.candidate.domain.Candidate;
import com.futurescope.platform.candidate.repository.CandidateRepository;
import com.futurescope.platform.candidate.service.AvatarUploadService;
import com.futurescope.platform.candidate.service.ProfileService;
import com.futurescope.platform.candidate.service.SavedJobService;
import com.futurescope.platform.candidate.web.dto.CandidateProfileResponse;
import com.futurescope.platform.candidate.web.dto.ProfileUpdateRequest;
import com.futurescope.platform.candidate.web.dto.SavedJobResponse;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.core.io.PathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/me")
public class MeController {

    private final CandidateRepository candidateRepository;
    private final ApplicationQueryService applicationQueryService;
    private final SavedJobService savedJobService;
    private final ProfileService profileService;
    private final AvatarUploadService avatarUploadService;

    public MeController(CandidateRepository candidateRepository,
                        ApplicationQueryService applicationQueryService,
                        SavedJobService savedJobService,
                        ProfileService profileService,
                        AvatarUploadService avatarUploadService) {
        this.candidateRepository = candidateRepository;
        this.applicationQueryService = applicationQueryService;
        this.savedJobService = savedJobService;
        this.profileService = profileService;
        this.avatarUploadService = avatarUploadService;
    }

    @GetMapping
    public ResponseEntity<CandidateProfileResponse> getProfile(@AuthenticationPrincipal User currentUser) {
        if (!"candidate".equals(currentUser.getUserType())) {
            throw new IllegalArgumentException("Not a candidate");
        }
        Candidate candidate = candidateRepository.findByUser(currentUser)
                .orElseThrow(() -> new IllegalArgumentException("Candidate profile not found"));
        CandidateProfileResponse response = new CandidateProfileResponse();
        response.setId(candidate.getId());
        response.setEmail(currentUser.getEmail());
        response.setFullName(candidate.getFullName());
        response.setPhone(candidate.getPhone());
        response.setCollege(candidate.getCollege());
        response.setGraduationYear(candidate.getGraduationYear());
        if (candidate.getAvatarStoragePath() != null && !candidate.getAvatarStoragePath().isBlank()) {
            response.setAvatarUrl("/me/avatar");
        }
        return ResponseEntity.ok(response);
    }

    @PatchMapping
    public ResponseEntity<CandidateProfileResponse> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ProfileUpdateRequest request
    ) {
        Candidate candidate = profileService.updateProfile(currentUser, request);
        CandidateProfileResponse response = new CandidateProfileResponse();
        response.setId(candidate.getId());
        response.setEmail(currentUser.getEmail());
        response.setFullName(candidate.getFullName());
        response.setPhone(candidate.getPhone());
        response.setCollege(candidate.getCollege());
        response.setGraduationYear(candidate.getGraduationYear());
        if (candidate.getAvatarStoragePath() != null && !candidate.getAvatarStoragePath().isBlank()) {
            response.setAvatarUrl("/me/avatar");
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/avatar")
    public ResponseEntity<CandidateProfileResponse> uploadAvatar(
            @AuthenticationPrincipal User currentUser,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            avatarUploadService.uploadAvatar(currentUser, file);
        } catch (java.io.IOException e) {
            throw new IllegalArgumentException("Failed to save avatar: " + e.getMessage());
        }
        return getProfile(currentUser);
    }

    @GetMapping("/avatar")
    public ResponseEntity<Resource> getAvatar(@AuthenticationPrincipal User currentUser) {
        if (!"candidate".equals(currentUser.getUserType())) {
            return ResponseEntity.notFound().build();
        }
        Candidate candidate = candidateRepository.findByUser(currentUser)
                .orElse(null);
        if (candidate == null || candidate.getAvatarStoragePath() == null || candidate.getAvatarStoragePath().isBlank()) {
            return ResponseEntity.notFound().build();
        }
        Path path = avatarUploadService.resolveAvatarPath(candidate.getAvatarStoragePath());
        if (path == null) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new PathResource(path);
        String contentType = path.toString().toLowerCase().endsWith(".png") ? "image/png"
                : path.toString().toLowerCase().endsWith(".webp") ? "image/webp"
                : path.toString().toLowerCase().endsWith(".gif") ? "image/gif"
                : "image/jpeg";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CACHE_CONTROL, "private, max-age=3600")
                .body(resource);
    }

    @GetMapping("/applications")
    public ResponseEntity<List<ApplicationResponse>> getMyApplications(@AuthenticationPrincipal User currentUser) {
        List<ApplicationResponse> list = applicationQueryService.listForCandidate(currentUser);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/saved-jobs")
    public ResponseEntity<List<SavedJobResponse>> getSavedJobs(@AuthenticationPrincipal User currentUser) {
        List<SavedJobResponse> list = savedJobService.listForCandidate(currentUser);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/saved-jobs")
    public ResponseEntity<SavedJobResponse> saveJob(
            @AuthenticationPrincipal User currentUser,
            @RequestBody Map<String, UUID> body
    ) {
        UUID jobId = body.get("jobId");
        if (jobId == null) throw new IllegalArgumentException("jobId is required");
        SavedJobResponse saved = savedJobService.saveJob(jobId, currentUser);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/saved-jobs/{id}")
    public ResponseEntity<Void> unsaveJob(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id
    ) {
        savedJobService.unsaveJob(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
