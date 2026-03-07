package com.futurescope.platform.candidate.service;

import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.candidate.domain.Candidate;
import com.futurescope.platform.candidate.repository.CandidateRepository;
import com.futurescope.platform.candidate.web.dto.ProfileUpdateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final CandidateRepository candidateRepository;

    public ProfileService(CandidateRepository candidateRepository) {
        this.candidateRepository = candidateRepository;
    }

    @Transactional
    public Candidate updateProfile(User currentUser, ProfileUpdateRequest request) {
        if (!"candidate".equals(currentUser.getUserType())) {
            throw new IllegalArgumentException("Not a candidate");
        }
        Candidate candidate = candidateRepository.findByUser(currentUser)
                .orElseThrow(() -> new IllegalArgumentException("Candidate profile not found"));

        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            candidate.setFullName(request.getFullName().trim());
        }
        if (request.getPhone() != null) {
            candidate.setPhone(request.getPhone().trim().isEmpty() ? null : request.getPhone().trim());
        }
        if (request.getCollege() != null) {
            candidate.setCollege(request.getCollege().trim().isEmpty() ? null : request.getCollege().trim());
        }
        if (request.getGraduationYear() != null) {
            candidate.setGraduationYear(request.getGraduationYear());
        }

        return candidateRepository.save(candidate);
    }
}
