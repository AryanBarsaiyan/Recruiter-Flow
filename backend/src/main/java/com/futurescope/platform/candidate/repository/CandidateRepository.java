package com.futurescope.platform.candidate.repository;

import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.candidate.domain.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CandidateRepository extends JpaRepository<Candidate, UUID> {

    Optional<Candidate> findByUser(User user);

}

