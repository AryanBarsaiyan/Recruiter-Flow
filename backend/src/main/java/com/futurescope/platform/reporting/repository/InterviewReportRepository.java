package com.futurescope.platform.reporting.repository;

import com.futurescope.platform.application.domain.JobApplication;
import com.futurescope.platform.interview.domain.Interview;
import com.futurescope.platform.reporting.domain.InterviewReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InterviewReportRepository extends JpaRepository<InterviewReport, UUID> {

    Optional<InterviewReport> findByInterview(Interview interview);

    Optional<InterviewReport> findByApplication(JobApplication application);
}
