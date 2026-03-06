package com.futurescope.platform.reporting.repository;

import com.futurescope.platform.reporting.domain.InterviewReport;
import com.futurescope.platform.reporting.domain.InterviewReportFactor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InterviewReportFactorRepository extends JpaRepository<InterviewReportFactor, UUID> {

    List<InterviewReportFactor> findByReport(InterviewReport report);
}
