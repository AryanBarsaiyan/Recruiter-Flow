package com.futurescope.platform.application.repository;

import com.futurescope.platform.application.domain.JobApplication;
import com.futurescope.platform.candidate.domain.Candidate;
import com.futurescope.platform.job.domain.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {

    Optional<JobApplication> findByJobAndCandidate(Job job, Candidate candidate);

    List<JobApplication> findByCandidateOrderByAppliedAtDesc(Candidate candidate);

    List<JobApplication> findByJobOrderByAppliedAtDesc(Job job);

    long countByJob(Job job);

    @Query("select count(a) from JobApplication a where a.job.company.id = :companyId")
    long countByJobCompanyId(@Param("companyId") UUID companyId);

}

