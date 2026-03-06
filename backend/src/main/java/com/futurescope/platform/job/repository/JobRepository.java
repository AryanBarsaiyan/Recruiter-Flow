package com.futurescope.platform.job.repository;

import com.futurescope.platform.auth.domain.Company;
import com.futurescope.platform.job.domain.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, UUID> {

    List<Job> findByCompany(Company company);

    Page<Job> findByCompanyOrderByCreatedAtDesc(Company company, Pageable pageable);

    long countByCompany(Company company);

    @Query("""
            select j from Job j
            where j.published = true
              and (j.applicationDeadline is null or j.applicationDeadline > :now)
            """)
    List<Job> findPublicOpenJobs(@Param("now") OffsetDateTime now);

}

