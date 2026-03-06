package com.futurescope.platform.candidate.web;

import com.futurescope.platform.application.service.ApplicationQueryService;
import com.futurescope.platform.application.web.dto.ApplicationResponse;
import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.candidate.domain.Candidate;
import com.futurescope.platform.candidate.repository.CandidateRepository;
import com.futurescope.platform.candidate.service.SavedJobService;
import com.futurescope.platform.candidate.web.dto.CandidateProfileResponse;
import com.futurescope.platform.candidate.web.dto.SavedJobResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/me")
public class MeController {

    private final CandidateRepository candidateRepository;
    private final ApplicationQueryService applicationQueryService;
    private final SavedJobService savedJobService;

    public MeController(CandidateRepository candidateRepository,
                        ApplicationQueryService applicationQueryService,
                        SavedJobService savedJobService) {
        this.candidateRepository = candidateRepository;
        this.applicationQueryService = applicationQueryService;
        this.savedJobService = savedJobService;
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
        return ResponseEntity.ok(response);
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
