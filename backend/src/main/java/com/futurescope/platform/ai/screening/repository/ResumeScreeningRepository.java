package com.futurescope.platform.ai.screening.repository;

import com.futurescope.platform.ai.screening.domain.ResumeScreening;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ResumeScreeningRepository extends JpaRepository<ResumeScreening, UUID> {
}

