package com.futurescope.platform.job.repository;

import com.futurescope.platform.auth.domain.Company;
import com.futurescope.platform.job.domain.Pipeline;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PipelineRepository extends JpaRepository<Pipeline, UUID> {

    List<Pipeline> findByCompany(Company company);

    Optional<Pipeline> findByCompanyAndIsDefaultTrue(Company company);

}

