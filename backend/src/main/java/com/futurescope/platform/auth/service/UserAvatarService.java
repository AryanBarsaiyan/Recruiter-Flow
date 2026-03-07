package com.futurescope.platform.auth.service;

import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserAvatarService {

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB
    private static final String[] ALLOWED_TYPES = { "image/jpeg", "image/png", "image/webp", "image/gif" };

    private final String uploadDir;
    private final UserRepository userRepository;

    public UserAvatarService(
            @Value("${app.avatar-upload.dir:./uploads/avatars}") String uploadDir,
            UserRepository userRepository
    ) {
        this.uploadDir = uploadDir;
        this.userRepository = userRepository;
    }

    @Transactional
    public String uploadAvatar(User currentUser, MultipartFile file) throws IOException {
        if ("candidate".equals(currentUser.getUserType())) {
            throw new IllegalArgumentException("Use /me/avatar for candidates");
        }

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Avatar file is required");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Avatar must be at most 2MB");
        }
        String contentType = Optional.ofNullable(file.getContentType()).orElse("");
        boolean allowed = false;
        for (String t : ALLOWED_TYPES) {
            if (t.equals(contentType)) {
                allowed = true;
                break;
            }
        }
        if (!allowed) {
            throw new IllegalArgumentException("Avatar must be JPEG, PNG, WebP or GIF");
        }

        Path base = Path.of(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(base);
        String ext = contentType.contains("png") ? "png" : contentType.contains("webp") ? "webp" : contentType.contains("gif") ? "gif" : "jpg";
        String storedName = "user_" + currentUser.getId() + "_" + UUID.randomUUID() + "." + ext;
        Path target = base.resolve(storedName);
        Files.copy(file.getInputStream(), target);

        String relativePath = storedName;
        currentUser.setAvatarStoragePath(relativePath);
        userRepository.save(currentUser);

        return relativePath;
    }

    public Path resolveAvatarPath(String storagePath) {
        if (storagePath == null || storagePath.isBlank()) return null;
        Path base = Path.of(uploadDir).toAbsolutePath().normalize();
        Path resolved = base.resolve(storagePath).normalize();
        if (!resolved.startsWith(base)) {
            return null;
        }
        return Files.exists(resolved) ? resolved : null;
    }
}
