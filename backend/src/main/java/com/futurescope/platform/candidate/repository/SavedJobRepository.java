package com.futurescope.platform.candidate.repository;

import com.futurescope.platform.candidate.domain.Candidate;
import com.futurescope.platform.candidate.domain.SavedJob;
import com.futurescope.platform.job.domain.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SavedJobRepository extends JpaRepository<SavedJob, UUID> {

    List<SavedJob> findByCandidateOrderByCreatedAtDesc(Candidate candidate);

    Optional<SavedJob> findByCandidateAndJob(Candidate candidate, Job job);
}
