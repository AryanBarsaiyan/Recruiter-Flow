package com.futurescope.platform.application.service;

import com.futurescope.platform.application.web.dto.UploadResumeResponse;
import com.futurescope.platform.job.repository.JobRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ResumeUploadService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final Pattern EMAIL = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    private static final Pattern PHONE = Pattern.compile("(?:\\(?\\+?[0-9]{1,3}\\)?[-. ]?)?(?:\\(?[0-9]{2,4}\\)?[-. ]?)+[0-9]{2,4}[-. ]?[0-9]{2,4}(?:[-. ]?[0-9]+)?");

    private final String uploadDir;
    private final JobRepository jobRepository;

    public ResumeUploadService(
            @Value("${app.resume-upload.dir:./uploads/resumes}") String uploadDir,
            JobRepository jobRepository
    ) {
        this.uploadDir = uploadDir;
        this.jobRepository = jobRepository;
    }

    public UploadResumeResponse uploadResume(UUID jobId, MultipartFile file) throws IOException {
        if (!jobRepository.existsById(jobId)) {
            throw new IllegalArgumentException("Job not found");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Resume file is required");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Resume file must be at most 5MB");
        }
        String originalFilename = Optional.ofNullable(file.getOriginalFilename()).orElse("resume");
        String ext = originalFilename.contains(".") ? originalFilename.substring(originalFilename.lastIndexOf('.')) : "";
        if (!".pdf".equalsIgnoreCase(ext) && !".txt".equalsIgnoreCase(ext)) {
            throw new IllegalArgumentException("Resume must be PDF or TXT");
        }

        Path base = Path.of(uploadDir).toAbsolutePath().normalize();
        Path jobDir = base.resolve(jobId.toString());
        Files.createDirectories(jobDir);
        String safeName = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        String storedName = UUID.randomUUID() + "_" + safeName;
        Path target = jobDir.resolve(storedName);
        Files.copy(file.getInputStream(), target);

        String relativePath = jobId + "/" + storedName;

        String rawText = extractText(file, ext);
        UploadResumeResponse response = new UploadResumeResponse();
        response.setStoragePath(relativePath);
        response.setOriginalFilename(originalFilename);
        if (rawText != null && !rawText.isBlank()) {
            response.setExtractedEmail(firstMatch(EMAIL, rawText));
            response.setExtractedPhone(firstMatch(PHONE, rawText));
            response.setExtractedFullName(guessFullName(rawText));
        }
        return response;
    }

    private String extractText(MultipartFile file, String ext) throws IOException {
        if (".pdf".equalsIgnoreCase(ext)) {
            try (PDDocument doc = Loader.loadPDF(file.getBytes())) {
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(doc);
            }
        }
        if (".txt".equalsIgnoreCase(ext)) {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        }
        return null;
    }

    private static String firstMatch(Pattern pattern, String text) {
        Matcher m = pattern.matcher(text);
        return m.find() ? m.group().trim() : null;
    }

    private static String guessFullName(String text) {
        String[] lines = text.lines().map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new);
        for (String line : lines) {
            if (line.length() >= 2 && line.length() <= 80 && !line.contains("@") && !PHONE.matcher(line).find()) {
                return line;
            }
        }
        return null;
    }

    /**
     * Resolves the storage path to an absolute file path for serving.
     * Returns null if path is invalid or file does not exist.
     */
    public Path resolveResumePath(String storagePath) {
        if (storagePath == null || storagePath.isBlank()) return null;
        Path base = Path.of(uploadDir).toAbsolutePath().normalize();
        Path resolved = base.resolve(storagePath).normalize();
        if (!resolved.startsWith(base)) {
            return null;
        }
        return Files.exists(resolved) ? resolved : null;
    }
}
