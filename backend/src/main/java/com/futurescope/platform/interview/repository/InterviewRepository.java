package com.futurescope.platform.interview.repository;

import com.futurescope.platform.application.domain.JobApplication;
import com.futurescope.platform.interview.domain.Interview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InterviewRepository extends JpaRepository<Interview, UUID> {

    List<Interview> findByApplication(JobApplication application);
}
