package com.futurescope.platform.auth.web;

import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.auth.service.UserAvatarService;
import com.futurescope.platform.auth.service.UserProfileService;
import com.futurescope.platform.auth.web.dto.UserProfileResponse;
import com.futurescope.platform.auth.web.dto.UserProfileUpdateRequest;
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

@RestController
@RequestMapping("/recruiter/me")
public class RecruiterMeController {

    private final UserProfileService userProfileService;
    private final UserAvatarService userAvatarService;

    public RecruiterMeController(UserProfileService userProfileService, UserAvatarService userAvatarService) {
        this.userProfileService = userProfileService;
        this.userAvatarService = userAvatarService;
    }

    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal User currentUser) {
        ensureRecruiterOrAdmin(currentUser);
        UserProfileResponse response = new UserProfileResponse();
        response.setEmail(currentUser.getEmail());
        response.setFullName(currentUser.getFullName());
        if (currentUser.getAvatarStoragePath() != null && !currentUser.getAvatarStoragePath().isBlank()) {
            response.setAvatarUrl("/recruiter/me/avatar");
        }
        return ResponseEntity.ok(response);
    }

    @PatchMapping
    public ResponseEntity<UserProfileResponse> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UserProfileUpdateRequest request
    ) {
        ensureRecruiterOrAdmin(currentUser);
        User updated = userProfileService.updateProfile(currentUser, request);
        UserProfileResponse response = new UserProfileResponse();
        response.setEmail(updated.getEmail());
        response.setFullName(updated.getFullName());
        if (updated.getAvatarStoragePath() != null && !updated.getAvatarStoragePath().isBlank()) {
            response.setAvatarUrl("/recruiter/me/avatar");
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/avatar")
    public ResponseEntity<UserProfileResponse> uploadAvatar(
            @AuthenticationPrincipal User currentUser,
            @RequestParam("file") MultipartFile file
    ) {
        ensureRecruiterOrAdmin(currentUser);
        try {
            userAvatarService.uploadAvatar(currentUser, file);
        } catch (java.io.IOException e) {
            throw new IllegalArgumentException("Failed to save avatar: " + e.getMessage());
        }
        return getProfile(currentUser);
    }

    @GetMapping("/avatar")
    public ResponseEntity<Resource> getAvatar(@AuthenticationPrincipal User currentUser) {
        ensureRecruiterOrAdmin(currentUser);
        if (currentUser.getAvatarStoragePath() == null || currentUser.getAvatarStoragePath().isBlank()) {
            return ResponseEntity.notFound().build();
        }
        Path path = userAvatarService.resolveAvatarPath(currentUser.getAvatarStoragePath());
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

    private void ensureRecruiterOrAdmin(User user) {
        String type = user.getUserType();
        if (!"recruiter".equals(type) && !"company_admin".equals(type) && !"platform_admin".equals(type)) {
            throw new IllegalArgumentException("Not a recruiter or admin");
        }
    }
}
