package com.futurescope.platform.candidate.service;

import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.candidate.domain.Candidate;
import com.futurescope.platform.candidate.domain.SavedJob;
import com.futurescope.platform.candidate.repository.CandidateRepository;
import com.futurescope.platform.candidate.repository.SavedJobRepository;
import com.futurescope.platform.candidate.web.dto.SavedJobResponse;
import com.futurescope.platform.job.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SavedJobService {

    private final SavedJobRepository savedJobRepository;
    private final CandidateRepository candidateRepository;
    private final JobRepository jobRepository;

    public SavedJobService(SavedJobRepository savedJobRepository,
                           CandidateRepository candidateRepository,
                           JobRepository jobRepository) {
        this.savedJobRepository = savedJobRepository;
        this.candidateRepository = candidateRepository;
        this.jobRepository = jobRepository;
    }

    @Transactional(readOnly = true)
    public List<SavedJobResponse> listForCandidate(User currentUser) {
        Candidate candidate = candidateRepository.findByUser(currentUser)
                .orElseThrow(() -> new IllegalArgumentException("Candidate profile not found"));
        return savedJobRepository.findByCandidateOrderByCreatedAtDesc(candidate).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public SavedJobResponse saveJob(UUID jobId, User currentUser) {
        Candidate candidate = candidateRepository.findByUser(currentUser)
                .orElseThrow(() -> new IllegalArgumentException("Candidate profile not found"));
        var job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));
        SavedJob existing = savedJobRepository.findByCandidateAndJob(candidate, job).orElse(null);
        if (existing != null) return toResponse(existing);
        SavedJob saved = new SavedJob();
        saved.setId(UUID.randomUUID());
        saved.setCandidate(candidate);
        saved.setJob(job);
        saved.setCreatedAt(OffsetDateTime.now());
        savedJobRepository.save(saved);
        return toResponse(saved);
    }

    @Transactional
    public void unsaveJob(UUID savedJobId, User currentUser) {
        Candidate candidate = candidateRepository.findByUser(currentUser)
                .orElseThrow(() -> new IllegalArgumentException("Candidate profile not found"));
        SavedJob saved = savedJobRepository.findById(savedJobId)
                .orElseThrow(() -> new IllegalArgumentException("Saved job not found"));
        if (!saved.getCandidate().getId().equals(candidate.getId())) {
            throw new IllegalArgumentException("Saved job not found");
        }
        savedJobRepository.delete(saved);
    }

    private SavedJobResponse toResponse(SavedJob s) {
        SavedJobResponse r = new SavedJobResponse();
        r.setId(s.getId());
        r.setJobId(s.getJob().getId());
        r.setJobTitle(s.getJob().getTitle());
        r.setSavedAt(s.getCreatedAt());
        return r;
    }
}
